package io.lumine.mythic.lib.data;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.ProfileProvider;
import fr.phoenixdevt.profiles.placeholder.PlaceholderProcessor;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.session.SessionUpdateEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.data.queue.DataLoadQueue;
import io.lumine.mythic.lib.data.queue.DataSaveQueue;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.profile.ProfileSessionState;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A general player data manager which implements
 * - player data caching on login
 * - support for both YAML and SQL
 * - async data loading and saving
 * - database contention management through queues
 * - better SQL data synchronization between servers
 * - profile-based data saving for MMOProfiles
 * - support for classic and proxy-mode profile selection
 *
 * @param <H> Type of player data being cached on login
 * @param <O> This is used to manipulate player data when players are offline
 * @author jules
 */
public abstract class SynchronizedDataManager<H extends SynchronizedDataHolder, O extends OfflineDataHolder> implements Closeable {
    private final MMOPlugin owning;
    private final Map<UUID, H> activeData = Collections.synchronizedMap(new HashMap<>());

    private Database<H, O> database;
    private DataSaveQueue<H> saveQueue;
    private DataLoadQueue<H> loadQueue;

    public SynchronizedDataManager(@NotNull MMOPlugin owning) {
        this.owning = Objects.requireNonNull(owning, "Plugin cannot be null");
    }

    @NotNull
    public Database<H, O> getDatabase() {
        return Objects.requireNonNull(database, "Database not setup");
    }

    public void setupDatabase(@NotNull Supplier<Database<H, O>> sql, @NotNull Supplier<Database<H, O>> fallback) {
        final var isSql = this.owning.getConfig().getBoolean("mysql.enabled");
        if (isSql) setupDatabase(sql.get());
        else setupDatabase(fallback.get());
    }

    public void setupDatabase(@NotNull Database<H, O> database) {
        Validate.isTrue(this.database == null, "Database is already set");

        this.database = Objects.requireNonNull(database, "Database cannot be null");

        this.saveQueue = new DataSaveQueue<>(this);
        this.loadQueue = new DataLoadQueue<>(this);

        Tasks.runAsync(owning, () -> {
            //UtilityMethods.debug(owning, "Data", "Applying database migrations...");
            this.database.setup();

            Bukkit.getScheduler().runTaskAsynchronously(owning, saveQueue);
            Bukkit.getScheduler().runTaskAsynchronously(owning, loadQueue);
            UtilityMethods.debug(owning, "Data", "Database ready");
        });
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
    @SuppressWarnings("unchecked")
    public O getOffline(UUID uuid) {
        return isLoaded(uuid) ? (O) activeData.get(uuid) : database.getOffline(uuid);
    }

    public void autosave() {
        for (var playerData : getLoaded())
            if (playerData.getMMOPlayerData().isPlaying()) saveData(playerData, SessionUpdateReason.AUTOSAVE);
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
     */
    public void initialize() {

        // Setup online player data
        Bukkit.getOnlinePlayers().forEach(this::onEnable);

        // Auto-save
        if (owning.getConfig().getBoolean("auto-save.enabled")) new AutoSaveRunnable(this);

        // Setup empty player data on login
        // Load data on login for profile plugins
        UtilityMethods.registerEvent(PlayerJoinEvent.class, FICTIVE_LISTENER, EventPriority.LOWEST, this::onJoin, owning, false);

        // Load data on session creation for non-profile plugins
        // Save data on session termination for non-profile plugins
        // Garbage collect player data on session death for non-profile plugins
        if (!owning.isProfilePlugin()) {
            UtilityMethods.registerEvent(SessionUpdateEvent.class, FICTIVE_LISTENER, EventPriority.NORMAL, this::onSessionUpdate, owning, false);
        }

        // Garbage collect player data on quit for profile plugins
        // Save data on logout for profile plugins
        UtilityMethods.registerEvent(PlayerQuitEvent.class, FICTIVE_LISTENER, EventPriority.MONITOR, this::onQuit, owning, false);

        // ProfileAPI compatibility
        if (!owning.isProfilePlugin() && MythicLib.plugin.hasProfiles()) {
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

    //region Event listeners

    protected void onQuit(PlayerQuitEvent event) {
        final var playerData = this.activeData.remove(event.getPlayer().getUniqueId());
        Validate.notNull(playerData, "Player data is not loaded");

        // Profile plugins require on-logout sync
        if (owning.isProfilePlugin() && playerData.isSessionReady()) {
            this.saveData(playerData, SessionUpdateReason.LOG_OUT);
        }
    }

    protected void onJoin(PlayerJoinEvent event) {
        this.onEnable(event.getPlayer());
    }

    protected void onEnable(Player player) {
        final var playerData = setup(player);

        // Profile plugins require on-login sync
        if (owning.isProfilePlugin()) {
            Validate.isTrue(!playerData.isSessionReady(), "Player data already loaded");
            // In proxy-mode, need to know official ID to load profile data.
            if (MythicLib.plugin.getProfileMode() != ProfileMode.PROXY) this.loadData(playerData);
        }
    }

    protected void onSessionUpdate(SessionUpdateEvent event) {

        // Session opening -> load data
        if (event.getNewState() == ProfileSessionState.OPENING) {
            final var playerData = get(event.getPlayerData().getUniqueId());
            Validate.isTrue(!playerData.isSessionReady(), "Player data already loaded");
            this.loadData(playerData);
        }

        // Session closing -> save data
        // Also unregister player data if needed
        else if (event.getNewState().isClosing()) {
            final var playerData = get(event.getPlayerData().getUniqueId());
            if (playerData.isSessionReady()) {
                this.saveData(playerData, event.getReason());
            }
        }

        // Session death -> switch player data
        else if (event.getNewState().isDead()) {
            this.activeData.compute(event.getPlayerData().getUniqueId(), (ignore, current) -> {

                // If player disconnected, do not keep.
                if (current == null) return null;

                // If player data is still valid, replace with new one
                return newPlayerData(current.getMMOPlayerData());
            });
        }
    }

    //endregion

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

        // Save data of online players
        for (var holder : getLoaded())
            if (holder.isSessionReady()) this.saveData(holder, SessionUpdateReason.LOG_OUT);

        // Stop queues
        this.loadQueue.end();
        this.saveQueue.end();

        // Wait for completion
        // This might not be needed on all server software. Not sure about that.
        this.loadQueue.sleepUntilCompletion();
        this.saveQueue.sleepUntilCompletion();

        // Release resources from data handler
        database.close();
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
     */
    @NotNull
    public CompletableFuture<Void> loadData(@NotNull H playerData) {
        return this.loadQueue.enqueue(playerData);
    }

    /**
     * @param playerData Player data to be saved.
     * @return Completable future that completes when the data is saved.
     */
    @NotNull
    public CompletableFuture<Void> saveData(@NotNull H playerData, @NotNull SessionUpdateReason reason) {
        playerData.onSaved(reason);
        return this.saveQueue.enqueue(playerData, reason);
    }

    /**
     * Called when a player logs in, loading the player data inside the map.
     * <p>
     * For YAML configs or SQL databases, data is loaded sync as not to overload
     * the main thread with SQL requests. Therefore, the object returned by that
     * function is always empty.
     *
     * @param player Player who just logged in
     * @return The empty player data, which will be loaded in the near future.
     */
    @NotNull
    public H setup(@NotNull Player player) {
        return activeData.computeIfAbsent(player.getUniqueId(), ignore -> newPlayerData(MMOPlayerData.setup(player)));
    }

    @NotNull
    public H setup(@NotNull UUID uniqueId) {
        return activeData.computeIfAbsent(uniqueId, ignore -> newPlayerData(MMOPlayerData.setup(uniqueId)));
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
    public void initialize(@NotNull EventPriority joinEventPriority, @NotNull EventPriority quitEventPriority) {
        initialize();
    }

    @Deprecated
    public void fake(H data) {
        activeData.put(data.getUniqueId(), data);
    }

    @Deprecated
    public SynchronizedDataManager(@NotNull MMOPlugin owning, boolean profilePlugin) {
        this(owning);
    }

    @Deprecated
    public H unregister(Player player, SaveReason reason) {
        return this.unregister(player, reason.adapt());
    }

    @Deprecated
    public H unregister(Player player, SessionUpdateReason reason) {

        final H playerData;
        if (reason == SessionUpdateReason.LOG_OUT) playerData = activeData.remove(player.getUniqueId());
        else if (reason == SessionUpdateReason.QUIT_PROFILE || reason == SessionUpdateReason.SWITCH_PROFILE)
            playerData = activeData.put(player.getUniqueId(), newPlayerData(MMOPlayerData.get(player.getUniqueId())));
        else throw new IllegalArgumentException("Unhandled save reason " + reason);
        Validate.notNull(playerData, "Could not find player data of player " + player.getUniqueId());

        return playerData;
    }

    @Deprecated
    public H setupEmpty(@NotNull Player player) {
        return setup(player);
    }

    @Deprecated
    public void saveAll(boolean autosave) {
        if (autosave) autosave();
        else close();
    }

    //endregion
}
