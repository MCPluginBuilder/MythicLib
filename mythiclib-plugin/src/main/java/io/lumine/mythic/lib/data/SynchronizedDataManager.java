package io.lumine.mythic.lib.data;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.ProfileProvider;
import fr.phoenixdevt.profiles.placeholder.PlaceholderProcessor;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.profile.LegacyProfiles;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.util.Autosaveable;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.util.Tasks;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * A general player data manager which implements
 * - player data caching on login
 * - support for both YAML and SQL
 * - better SQL data synchronization between servers
 * - profile-based data saving for MMOProfiles
 *
 * @param <H> Type of player data being cached on login
 * @param <O> This is used to manipulate player data when players
 *            are offline
 * @author jules
 */
public abstract class SynchronizedDataManager<H extends SynchronizedDataHolder, O extends OfflineDataHolder> implements Closeable {
    private final JavaPlugin owning;
    private final Map<UUID, H> activeData = Collections.synchronizedMap(new HashMap<>());

    private final boolean profilePlugin;

    @NotNull
    private SynchronizedDataHandler<H, O> dataHandler;

    public SynchronizedDataManager(@NotNull JavaPlugin owning, @NotNull SynchronizedDataHandler<H, O> dataHandler) {
        this(owning, dataHandler, false);
    }

    public SynchronizedDataManager(@NotNull JavaPlugin owning, @NotNull SynchronizedDataHandler<H, O> dataHandler, boolean profilePlugin) {
        this.owning = Objects.requireNonNull(owning, "Plugin cannot be null");
        this.dataHandler = Objects.requireNonNull(dataHandler, "Data handler cannot be null");
        this.profilePlugin = profilePlugin;
    }

    public void setDataHandler(@NotNull SynchronizedDataHandler<H, O> dataHandler) {
        this.dataHandler = Objects.requireNonNull(dataHandler, "Data handler cannot be null");

        dataHandler.setup();
    }

    @NotNull
    public SynchronizedDataHandler<H, O> getDataHandler() {
        return dataHandler;
    }

    @NotNull
    public JavaPlugin getOwningPlugin() {
        return owning;
    }

    @NotNull
    public H get(OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    /**
     * Gets the player data, or throws an exception if not found.
     * The player data should be loaded when the player logs in
     * so it's really bad practice to setup the player data if it's not loaded.
     *
     * @param uuid Player UUID
     * @return Player data, if it's loaded
     */
    @NotNull
    public H get(UUID uuid) {
        return Objects.requireNonNull(activeData.get(uuid), "Player data is not loaded");
    }

    @Nullable
    public H getOrNull(OfflinePlayer player) {
        return getOrNull(player.getUniqueId());
    }

    @Nullable
    public H getOrNull(UUID uuid) {
        return activeData.get(uuid);
    }

    /**
     * Offline player data is used to handle processes like friend removal
     * which can still occur if one of the two players is offline.
     * <p>
     * Unlike {@link #get(UUID)} this method never returns a null instance
     *
     * @param uuid Player unique id
     * @return Offline player data
     */
    @NotNull
    public O getOffline(UUID uuid) {
        return isLoaded(uuid) ? (O) activeData.get(uuid) : dataHandler.getOffline(uuid);
    }

    public void autosave() {
        for (H holder : getLoaded())
            if (holder.isSynchronized()) {
                if (holder instanceof Autosaveable) ((Autosaveable) holder).prepareSaving(true);
                // TODO Batching
                Tasks.runAsync(owning, () -> getDataHandler().saveData(holder, true));
            }
    }

    private static final Listener FICTIVE_LISTENER = new Listener() {
    };

    /**
     * This method is called when the plugin enables and does three things:
     * - initialize player data of currently connected players. This makes the plugin
     * support the /reload command.
     * - register the join and quit events which are required to load and unload data
     * at the right time. By manipulating the event priority, you can choose which
     * plugin load their data first.
     * MythicLib > MMOProfiles > MMOCore > MMOItems/MMOInventory
     * - enable auto-save if found in the configuration file
     *
     * @param joinEventPriority Event priority when logging in
     * @param quitEventPriority Event priority when logging off
     */
    public void initialize(@NotNull EventPriority joinEventPriority, @NotNull EventPriority quitEventPriority) {

        // Setup online player data
        Bukkit.getOnlinePlayers().forEach(this::setup);

        // Auto-save
        if (owning.getConfig().getBoolean("auto-save.enabled")) new AutoSaveRunnable(this);

        // Setup data on login
        UtilityMethods.registerEvent(PlayerJoinEvent.class, FICTIVE_LISTENER, joinEventPriority, event -> setup(event.getPlayer()), owning, false);

        // Save data on logout
        if (profilePlugin || MythicLib.plugin.getProfileMode() != ProfileMode.LEGACY)
            UtilityMethods.registerEvent(PlayerQuitEvent.class, FICTIVE_LISTENER, quitEventPriority, event -> unregister(event.getPlayer()), owning, false);

        // ProfileAPI compatibility
        if (!profilePlugin && MythicLib.plugin.hasProfiles()) {
            owning.getLogger().log(Level.INFO, "Hooked onto ProfileAPI");

            // Placeholders for MMOProfiles
            final ProfileProvider profilePlugin = Bukkit.getServicesManager().getRegistration(ProfileProvider.class).getProvider();
            final ProfileDataModule module = (ProfileDataModule) newProfileDataModule();
            if (module instanceof PlaceholderProcessor)
                profilePlugin.registerPlaceholders((PlaceholderProcessor) module);

            // Support for legacy profiles
            if (MythicLib.plugin.getProfileMode() == ProfileMode.LEGACY)
                LegacyProfiles.hook(profilePlugin, module, this, FICTIVE_LISTENER, joinEventPriority, quitEventPriority);
        }
    }

    /**
     * Saves all currently loaded data. It is either used on server
     * shutdown, which requires to save all the data of currently
     * connected players, or when performing frequent autosaves.
     * <p>
     * On server shutdown (/restart) pending async methods must be
     * completed before the program stops, otherwise data saving
     * tasks are lost, deleting the players' progressions.
     */
    @Override
    public void close() {

        // Save data SYNCHRONOUSLY of online players (not called with /restart)
        for (H holder : getLoaded())
            if (holder.isSynchronized()) {
                if (holder instanceof Closeable) ((Closeable) holder).close();
                getDataHandler().saveData(holder, false);
            }

        // Wait for completion of safe tasks
        owning.getLogger().log(Level.INFO, "Waiting for other threads, please wait...");
        Tasks.executePendingSafe(owning);
        Tasks.waitSafe(owning);

        // Release resources from data handler
        dataHandler.close();
    }

    /**
     * @param playerData Empty player data which will be populated.
     * @return Completable future that completes when the data is loaded.
     * It will only complete if the following conditions are met:
     * <ul>
     * <li>data is loaded successfully</li>
     * <li>player is still online when the worked thread is done loading data</li>
     * </ul>
     * If the player left the server while data was being loaded, player data will be re-saved if necessary.
     */
    @NotNull
    public CompletableFuture<Void> loadData(@NotNull H playerData) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final AtomicBoolean success = new AtomicBoolean();
        Tasks.runAsync(owning, () -> success.set(dataHandler.loadData(playerData))).thenAccept(Tasks.sync(owning, ignored -> {

            // No success, do nothing
            if (!success.get()) return;

            // If player has logged off in the meantime, save data and do not complete
            if (!playerData.getMMOPlayerData().isLookup() && !playerData.getMMOPlayerData().isOnline()) {
                saveData(playerData);
                return;
            }

            future.complete(null);
        }));
        return future;
    }

    /**
     * @param playerData Player data to be saved.
     * @return Completable future that completes when the data is loaded.
     */
    public CompletableFuture<Void> saveData(@NotNull H playerData) {
        return Tasks.runSafeAsync(owning, () -> dataHandler.saveData(playerData, false));
    }

    /**
     * Called when a player logs in, loading the player data inside the map.
     * <p>
     * For YAML configs or SQL databases, data is loaded sync as not to overload
     * the main thread with SQL requests. Therefore, the object returned by that
     * function is always empty.
     *
     * @param player Player UUID (not profile)
     * @return The empty player data, which will be loaded in a near future.
     */
    public H setup(@NotNull Player player) {

        // Get data or compute it if non existent (more resilient)
        final @NotNull H playerData = activeData.computeIfAbsent(player.getUniqueId(), uuid -> newPlayerData(MMOPlayerData.get(player.getUniqueId())));

        // Schedule data loading
        if (requiresSynchronizationOnLogin(playerData))
            loadData(playerData).thenAccept(Tasks.sync(owning, v -> {
                playerData.markAsSynchronized();
                Bukkit.getPluginManager().callEvent(new SynchronizedDataLoadEvent(this, playerData));
            }));

        return playerData;
    }

    private boolean requiresSynchronizationOnLogin(@NotNull H holder) {

        // Safety - should never happen
        if (holder.isSynchronized()) return false;

        // Profile plugins already require sync
        if (profilePlugin) return true;

        // No profile plugin - sync like usual
        if (MythicLib.plugin.getProfileMode() == null) return true;

        // Wait for the player to choose their profile
        if (MythicLib.plugin.getProfileMode() == ProfileMode.LEGACY) return false;

        // Only if the player has chosen their profile
        if (MythicLib.plugin.getProfileMode() == ProfileMode.PROXY) return holder.getMMOPlayerData().hasProfile();

        // Unhandled profile mode
        throw new RuntimeException("Unhandled profile mode");
    }

    /**
     * Safely unregisters the player data from the map.
     * This saves the player data either through SQL or YAML,
     * then closes the player data and clears it from the data map.
     *
     * @param player Player whose data needs to be unregistered
     * @return Completable future of the data being saved
     */
    @NotNull
    public CompletableFuture<Void> unregister(@NotNull Player player) {
        final H playerData = activeData.remove(player.getUniqueId());
        Validate.notNull(playerData, "Could not find player data of player '" + player.getUniqueId() + "'");

        // Close and unregister data instantly if no error occurred
        if (playerData instanceof Closeable) ((Closeable) playerData).close();

        // Save data if required
        return playerData.isSynchronized() ? saveData(playerData) : CompletableFuture.completedFuture(null);
    }

    /**
     * @param playerData Data of player who just logged in
     * @return A new instance of player data
     */
    public abstract H newPlayerData(@NotNull MMOPlayerData playerData);

    /**
     * @return An object of type {@link fr.phoenixdevt.profiles.ProfileDataModule} which is an object
     * that cannot be referenced inside of that class to avoid import issues.
     */
    public abstract Object newProfileDataModule();

    public boolean isLoaded(UUID uuid) {
        return activeData.containsKey(uuid);
    }

    public Collection<H> getLoaded() {
        return activeData.values();
    }

    @Deprecated
    public void saveAll(boolean autosave) {
        if (autosave) autosave();
        else close();
    }

    @Deprecated
    public void unregisterSafely(H playerData) {
        unregister(playerData.getPlayer());
    }
}
