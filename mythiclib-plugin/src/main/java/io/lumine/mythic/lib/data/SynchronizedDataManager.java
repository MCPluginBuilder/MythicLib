package io.lumine.mythic.lib.data;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.ProfileProvider;
import fr.phoenixdevt.profiles.placeholder.PlaceholderProcessor;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.util.Tasks;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private final MMOPlugin owning;
    private final Map<UUID, H> activeData = Collections.synchronizedMap(new HashMap<>());

    private final boolean profilePlugin;

    @NotNull
    private SynchronizedDataHandler<H, O> dataHandler;

    public SynchronizedDataManager(@NotNull MMOPlugin owning, @NotNull SynchronizedDataHandler<H, O> dataHandler) {
        this(owning, dataHandler, false);
    }

    public SynchronizedDataManager(@NotNull MMOPlugin owning, @NotNull SynchronizedDataHandler<H, O> dataHandler, boolean profilePlugin) {
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
    public MMOPlugin getOwningPlugin() {
        return owning;
    }

    @NotNull
    public H get(OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    /**
     * Gets the player data, or throws an exception if not found.
     * The player data should be loaded when the player logs in
     * so it's bad practice to set up the player data if it's not loaded.
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

        // TODO Batching
        for (H holder : getLoaded())
            if (holder.isSessionReady()) {
                holder.onSaved(SaveReason.AUTOSAVE);
                Tasks.runAsync(owning, () -> dataHandler.saveData(holder, SaveReason.AUTOSAVE));
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
            UtilityMethods.registerEvent(PlayerQuitEvent.class, FICTIVE_LISTENER, quitEventPriority, event -> unregister(event.getPlayer(), SaveReason.LOG_OUT), owning, false);

        // ProfileAPI compatibility
        if (!profilePlugin && MythicLib.plugin.hasProfiles()) {
            owning.getLogger().log(Level.INFO, "Hooked onto ProfileAPI");

            // Placeholders for MMOProfiles
            final ProfileProvider profilePlugin = Bukkit.getServicesManager().getRegistration(ProfileProvider.class).getProvider();
            final ProfileDataModule module = (ProfileDataModule) newProfileDataModule();

            // Register profile data module
            profilePlugin.registerModule(module);

            // Register placeholders
            if (module instanceof PlaceholderProcessor)
                profilePlugin.registerPlaceholders((PlaceholderProcessor) module);
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
            if (holder.isSessionReady()) {
                holder.onSaved(SaveReason.LOG_OUT);
                dataHandler.saveData(holder, SaveReason.LOG_OUT);
                holder.markSessionClosed();
            }

        // Wait for completion of safe tasks
        owning.getLogger().log(Level.INFO, "Waiting for other threads, please wait...");
        Tasks.executePendingSafe(owning);
        Tasks.waitSafe(owning);

        // Release resources from data handler
        dataHandler.close();
    }

    @NotNull
    public CompletableFuture<Void> loadData(@NotNull H playerData) {
        return loadData(playerData, null);
    }

    /**
     * Loads data asynchronously from the data source and populates the provided
     * player data instance. If loaded, the player data is marked as ready, the
     * corresponding Bukkit event is called.
     *
     * @param playerData Empty player data which will be populated.
     * @return Completable future that completes when the data is loaded.
     *         It will only complete if the following conditions are met:
     *         <ul>
     *         <li>data is loaded successfully</li>
     *         <li>player is still online when the worked thread is done loading data</li>
     *         </ul>
     *         If the player left the server while data was being loaded, player data will be re-saved if necessary.
     * @see #saveData(SynchronizedDataHolder, SaveReason)
     */
    @NotNull
    public CompletableFuture<Void> loadData(@NotNull H playerData, @Nullable Event parentProfileEvent) {
        final var future = new CompletableFuture<Void>();
        final var lookup = playerData.getMMOPlayerData().isLookup();
        Tasks.runAsync(owning, () -> {

            // Load player data
            final var dataFetcher = new DataFetcher<>(this, playerData);
            final var result = dataFetcher.run();

            // No success, do nothing
            if (result.type != DataLoadResult.Type.SUCCESS) {
                future.complete(null);
                return;
            }

            // Complete sync
            Tasks.runSync(owning, () -> {
                if (!lookup) {
                    playerData.markSessionReady(); // Mark as ready
                    Bukkit.getPluginManager().callEvent(new SynchronizedDataLoadEvent(this, playerData, parentProfileEvent));
                }
                future.complete(null); // Complete future
            });
        });
        return future;
    }

    /**
     * @param playerData Player data to be saved.
     * @return Completable future that completes when the data is saved.
     * @see #loadData(SynchronizedDataHolder, Event)
     */
    @NotNull
    public CompletableFuture<Void> saveData(@NotNull H playerData, @NotNull SaveReason reason) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        Tasks.runAsync(owning, () -> {

            // Save data
            dataHandler.saveData(playerData, reason);

            // Complete sync
            Tasks.runSync(owning, () -> {
                playerData.markSessionClosed();
                future.complete(null);
            });
        });
        return future;
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
     * @see #unregister(Player, SaveReason)
     */
    public H setup(@NotNull Player player) {

        // Get data or compute it if non existent (more resilient)
        final @NotNull H playerData = activeData.computeIfAbsent(player.getUniqueId(), uuid -> newPlayerData(MMOPlayerData.get(player.getUniqueId())));

        // Schedule data loading
        if (requiresSynchronizationOnLogin(playerData)) loadData(playerData, null);

        return playerData;
    }

    private boolean requiresSynchronizationOnLogin(@NotNull H holder) {

        // Safety - should never happen
        if (holder.isSessionReady()) return false;

        // Profile plugins always require on-login sync
        if (profilePlugin) return true;

        // No profile plugin - sync like usual
        if (MythicLib.plugin.getProfileMode() == ProfileMode.NONE) return true;

        // Profile plugin detected => load if profile is provided!
        // In proxy mode, profile is loaded right on login
        // In legacy mode, depends on option "quit-profile-on-logout"
        return holder.getMMOPlayerData().hasProfile();
    }

    /**
     * @deprecated TODO find better alternative. the #unregister only calls on profileunload
     *         when using legacy profiles. if the player logs out without choosing a profile
     *         this event will not call, and the data will remain in memory => LEAK
     */
    @Deprecated(forRemoval = true)
    public void garbageCollect(@NotNull Player player) {
        activeData.remove(player.getUniqueId());
    }

    /**
     * Safely unregisters the player data from the map.
     * This saves the player data either through SQL or YAML,
     * then closes the player data and clears it from the data map.
     *
     * @param player Player whose data needs to be unregistered
     * @param reason Reason why the data is being saved
     * @return Completable future of the data being saved
     * @see #setup(Player)
     */
    @NotNull
    public CompletableFuture<Void> unregister(@NotNull Player player, @NotNull SaveReason reason) {
        final H playerData;
        if (reason == SaveReason.LOG_OUT) playerData = activeData.remove(player.getUniqueId());
        else if (reason == SaveReason.QUIT_PROFILE)
            playerData = activeData.put(player.getUniqueId(), newPlayerData(MMOPlayerData.get(player.getUniqueId())));
        else throw new IllegalArgumentException("Unhandled save reason " + reason);
        Validate.notNull(playerData, "Could not find player data of player '" + player.getUniqueId() + "'");

        // Session not ready
        if (!playerData.isSessionReady()) return CompletableFuture.completedFuture(null);

        playerData.onSaved(reason);
        return saveData(playerData, reason);
    }

    /**
     * @param playerData Data of player who just logged in
     * @return A new instance of player data
     */
    public abstract H newPlayerData(@NotNull MMOPlayerData playerData);

    public abstract void loadEmptyPlayerData(@NotNull H playerData);

    /**
     * @return An object of type {@link fr.phoenixdevt.profiles.ProfileDataModule} which is an object
     *         that cannot be referenced inside of that class to avoid import issues.
     */
    public abstract Object newProfileDataModule();

    public boolean isLoaded(UUID uuid) {
        return activeData.containsKey(uuid);
    }

    public Collection<H> getLoaded() {
        return activeData.values();
    }

    //region Deprecated

    @Deprecated
    public CompletableFuture<Void> saveData(@NotNull H playerData) {
        return saveData(playerData, SaveReason.LOG_OUT);
    }

    @Deprecated
    public CompletableFuture<Void> unregister(@NotNull Player player) {
        var future = new CompletableFuture<Void>();
        return unregister(player, SaveReason.LOG_OUT).thenApply(t -> {
            future.complete(null);
            return null;
        });
    }

    @Deprecated
    public void unregisterSafely(H playerData) {
        unregister(playerData.getPlayer(), SaveReason.LOG_OUT);
    }

    @Deprecated
    public void saveAll(boolean autosave) {
        if (autosave) autosave();
        else close();
    }

    //endregion
}
