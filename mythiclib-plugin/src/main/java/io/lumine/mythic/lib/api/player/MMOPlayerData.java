package io.lumine.mythic.lib.api.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.listener.PlayerListener;
import io.lumine.mythic.lib.message.actionbar.ActionBarHandler;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import io.lumine.mythic.lib.player.cooldown.CooldownType;
import io.lumine.mythic.lib.player.particle.ParticleEffectMap;
import io.lumine.mythic.lib.player.permission.PermissionMap;
import io.lumine.mythic.lib.player.potion.PermanentPotionEffectMap;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.player.skill.PassiveSkillMap;
import io.lumine.mythic.lib.player.skillmod.SkillModifierMap;
import io.lumine.mythic.lib.profile.ProfileSession;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import io.lumine.mythic.lib.rpg.provided.PlayerResourceData;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;

public class MMOPlayerData {

    public final AtomicInteger damageParticleCount = new AtomicInteger(0);
    public float lastAttackCooldown;

    // Information shared across all sessions
    private final ActionBarHandler actionBar = new ActionBarHandler(this);
    private final List<TemporaryHandler> tempHandlers = new ArrayList<>();
    private final VariableList variableList = new VariableList(VariableScope.PLAYER);

    /**
     * @param lookup   Is this player data beign used for database lookup?
     *                 This determines if is_saved should be switched back to 0 when data has been loaded
     *                 and whether MythicLib should try to use the profile UUID for data lookup.
     * @param uniqueId Player's unique ID, or UUID to lookup
     */
    public MMOPlayerData(boolean lookup, @NotNull UUID uniqueId) {
        this.lookup = lookup;
        this.entityId = Objects.requireNonNull(uniqueId, "UUID cannot be null");
        this.officialId = uniqueId;
    }

    /**
     * For backwards compatibility, this returns the same value as getPlayer().getUniqueId().
     * <p>
     * Developers are encouraged to use this method over other getters.
     *
     * @return The Player entity unique ID. This may differ from the current player's
     *         profile ID depending on the profile provider being used on the server.
     * @see #getProfileId()
     * @see #getOfficialId()
     * @see SynchronizedDataHolder#getEffectiveId()
     */
    @NotNull
    public UUID getUniqueId() {
        return entityId;
    }

    //region Player session

    /**
     * UUID of the Player entity. It has to be one of the
     * two among the profile and official player ID.
     */
    @NotNull
    private final UUID entityId;

    @Nullable
    private Player player;
    @Nullable
    private String lastPlayerName;

    private final boolean lookup;

    /**
     * Last time the player either logged in or logged out.
     */
    private long lastLogActivity;

    public boolean isLookup() {
        return lookup;
    }

    /**
     * @return The last time, in millis, the player logged in or out
     */
    public long getLastLogActivity() {
        return lastLogActivity;
    }

    @NotNull
    public String getPlayerName() {
        if (lastPlayerName == null) return this.entityId.toString();
        return lastPlayerName;
    }

    public boolean isTimedOut() {
        this.savedProfileSessions.values().removeIf(ProfileSession::isTimedOut);
        return !isOnline() && this.savedProfileSessions.isEmpty();
    }

    /**
     * This method simply checks if the cached Player instance is null
     * because MythicLib uncaches it when the player leaves for memory purposes.
     *
     * @return If the player is currently online.
     * @see #isPlaying()
     */
    public boolean isOnline() {
        // #isOnline is necessary as the Player instance is only cleared
        // 20 ticks after the player logs off to avoid 99% on-logout NPE's
        return player != null && player.isOnline();
    }

    /**
     * Throws an exception if the player is currently not online
     * OR if the Player object has not been provided yet.
     * <p>
     * MythicLib updates the Player instance on event priority LOW
     * using {@link PlayerJoinEvent} in class {@link PlayerListener}
     *
     * @return Returns the corresponding Player instance.
     */
    @NotNull
    public Player getPlayer() {
        return Objects.requireNonNull(player, "Player is offline");
    }

    /**
     * Caches a new Player instance and refreshes the last log activity.
     * Provided player can be null if the player is disconnecting. This
     * method might be called multiple times per player join/quit event.
     *
     * @param player Player instance to cache (null if logging off)
     */
    public void updatePlayer(@Nullable Player player) {

        // Player logging off
        if (player == null && this.player != null) {
            this.player = null;
            this.lastLogActivity = System.currentTimeMillis();
        }

        // Player logging in
        else if (player != null && this.player == null) {
            this.player = player;
            this.lastLogActivity = System.currentTimeMillis();
            this.lastPlayerName = player.getName();
        }
    }

    //endregion

    //region Profile session

    @NotNull
    private UUID officialId;

    @Nullable
    private ProfileSession profileSession;
    private boolean nextSessionBuffered;
    private SessionUpdateReason nextSessionReasonBuffer;
    @Nullable
    private UUID nextSessionProfileBuffer;

    private final Object sessionLock = new Object();

    /**
     * Watch out for the `null` key for when no profile is chosen!
     */
    private final Map<UUID, ProfileSession> savedProfileSessions = new HashMap<>();

    /**
     * This method will throw an error if the player hasn't chosen a profile
     * yet. It is also guaranteed to throw an error if proxy-based profiles
     * are not enabled.
     * <p>
     * Developers are discouraged from using this method.
     *
     * @return The official Mojang/Microsoft account UUID
     */
    @NotNull
    public UUID getOfficialId() {
        return officialId;
    }

    public void setOfficialId(@NotNull UUID officialId) {
        Validate.isTrue(MythicLib.plugin.getProfileMode() == ProfileMode.PROXY, "Player official IDs can only change in proxy profile mode");
        this.officialId = Objects.requireNonNull(officialId, "Official ID cannot be null");
    }

    /**
     * If support for the Profile API is enabled, this returns the current
     * player's profile ID. This method will throw an error if they have
     * not chosen a profile yet.
     *
     * @return The UUID of the current player's profile
     */
    @NotNull
    public UUID getProfileId() {
        return getProfileSession().getProfileId();
    }

    public boolean hasProfile() {
        ProfileSession session;
        synchronized (sessionLock) {
            session = profileSession;
        }
        return session != null && session.hasProfile();
    }

    public boolean isPlaying() {
        ProfileSession session;
        synchronized (sessionLock) {
            session = profileSession;
        }
        return session != null && session.isReady();
    }

    public void shutdownSession() {
        synchronized (sessionLock) {
            if (this.profileSession != null) {
                this.profileSession.shutdown();
                this.profileSession = null;
            }
        }
    }

    public void clearNextSessionBuffer() {
        synchronized (sessionLock) {
            nextSessionBuffered = false;
        }
    }

    public void applyNextSessionBuffer() {
        synchronized (sessionLock) {
            // Check for buffer after saving
            if (nextSessionBuffered) {
                nextSessionBuffered = false;
                chooseProfile(nextSessionProfileBuffer, nextSessionReasonBuffer); // Re-enter lock
            }
        }
    }

    /**
     * Saves the current profile session temporarily. If the player leaves this
     * session for too long, it will eventually be discarded.
     */
    public void saveCurrentProfileSession() {
        synchronized (sessionLock) {
            Validate.notNull(this.profileSession, "No profile session to save");
            Validate.isTrue(this.profileSession.isDead(), "Current profile session is still alive");

            final var mapKey = this.profileSession.hasProfile() ? this.profileSession.getProfileId() : null;
            this.savedProfileSessions.put(mapKey, this.profileSession);
            this.profileSession = null;
        }
    }

    /**
     * Looks for previous profile sessions with the same profile ID. If one
     * is found, restore the previous session with all its data. If none is
     * found, create a new profile session and open it.
     *
     * @param profileId ID of profile opened, or null if no profile
     */
    public void chooseProfile(@Nullable UUID profileId, @NotNull SessionUpdateReason reason) {
        Validate.isTrue(!lookup, "Cannot choose a profile in lookup mode");

        final String debugMessage;
        synchronized (sessionLock) {
            final ProfileSession restoredSession;

            // Buffer new session if previous one is not dead yet
            // Happens on re-login if previous session has not been saved yet
            if (this.profileSession != null) {
                nextSessionBuffered = true;
                nextSessionReasonBuffer = reason;
                nextSessionProfileBuffer = profileId;
                debugMessage = "Buffered session " + profileId + " for player " + getPlayerName() + ", current session dump = " + this.profileSession;
            }

            // Restore previous session
            else if ((restoredSession = savedProfileSessions.remove(profileId)) != null) {
                Validate.isTrue(this.profileSession == null, "Previous profile session is not dead");
                final var recycledSession = new ProfileSession(this, restoredSession);
                this.profileSession = recycledSession;
                recycledSession.initializeSession(reason);
                debugMessage = "Restored session " + profileId + " for player " + getPlayerName();
            }

            // Create new session
            else {
                Validate.isTrue(this.profileSession == null, "Previous profile session is not dead");
                final var newSession = new ProfileSession(this, profileId);
                this.profileSession = newSession;
                newSession.initializeSession(reason);
                debugMessage = "Initialized new session " + profileId + " for player " + getPlayerName();
            }
        }

        UtilityMethods.debug(MythicLib.plugin, "Session", debugMessage);
    }

    public boolean hasProfileSession() {
        ProfileSession session;
        synchronized (sessionLock) {
            session = profileSession;
        }
        return session != null;
    }

    /**
     * Atomically reads the player's profile session
     *
     * @return The player's profile session
     */
    @NotNull
    public ProfileSession getProfileSession() {
        ProfileSession session;
        synchronized (sessionLock) {
            session = profileSession;
        }
        return Objects.requireNonNull(session, "No profile chosen");
    }

    public void addTemporaryHandler(@NotNull TemporaryHandler handler) {
        Validate.notNull(handler, "Handler cannot be null");
        tempHandlers.add(handler);
    }

    public void removeTemporaryHandler(@NotNull TemporaryHandler handler) {
        Validate.notNull(tempHandlers.remove(handler), "Handler is not registered");
    }

    public void clearTemporaryHandlers() {
        tempHandlers.forEach(handler -> handler.closeNow(true));
        tempHandlers.clear();
    }

    //endregion

    //region Left Clicks

    /**
     * This is used to avoid calling left clicks on arm swings that were
     * already registered by the PlayerInteractEvent.
     */
    private long nextLeftClick;

    public void blockLeftClicks(long timeOut) {
        this.nextLeftClick = System.currentTimeMillis() + timeOut;
    }

    public boolean canLeftClick() {
        return System.currentTimeMillis() > nextLeftClick;
    }

    //endregion

    /**
     * Used by MMOItems on login to forcefully clear all
     * modifiers due to items. This acts as a safeguard in
     * case they were not cleared on player logout.
     */
    public void clearModifiers(@NotNull String key) {
        getStatMap().getInstances().forEach(ins -> ins.removeIf(key::equals));
        getSkillModifierMap().removeModifiers(key);
        getPermanentEffectMap().removeModifiers(key);
        getParticleEffectMap().removeModifiers(key);
        getPassiveSkillMap().removeModifiers(key);
        // cooldownMap: nothing needed
        getPermissionMap().removeModifiers(key);
        // variableList: nothing needed
    }

    //region Session data getters

    private final ProfileSession fallbackProfileSession = new ProfileSession(this, UUID.randomUUID());

    @NotNull
    protected ProfileSession safePlayerSession() {
        ProfileSession session;
        synchronized (sessionLock) {
            session = profileSession;
        }
        return Objects.requireNonNullElse(session, fallbackProfileSession);
    }

    /**
     * @return The player's stat map which can be used by any other plugins to
     *         apply stat modifiers to ANY MMOItems/MMOCore/external stats,
     *         calculate stat values, etc.
     */
    @NotNull
    public StatMap getStatMap() {
        return safePlayerSession().getStatMap();
    }

    /**
     * Cooldown maps centralize cooldowns in MythicLib for easier use.
     * Can be used for item cooldows, item abilities, MMOCore player
     * skills or any other external plugin
     *
     * @return The main player's cooldown map
     */
    @NotNull
    public CooldownMap getCooldownMap() {
        return safePlayerSession().getCooldownMap();
    }

    @NotNull
    public PlayerResourceData getResources() {
        return safePlayerSession().getResources();
    }

    /**
     * @return The player's skill modifier map. This map applies modifications
     *         to numerical skill parameters (damage, cooldown...)
     */
    @NotNull
    public SkillModifierMap getSkillModifierMap() {
        return safePlayerSession().getSkillModifierMap();
    }

    @NotNull
    public PermanentPotionEffectMap getPermanentEffectMap() {
        return safePlayerSession().getPermanentEffectMap();
    }

    @NotNull
    public ParticleEffectMap getParticleEffectMap() {
        return safePlayerSession().getParticleEffectMap();
    }

    /**
     * @return All currently registered player passive skills
     */
    @NotNull
    public PassiveSkillMap getPassiveSkillMap() {
        return safePlayerSession().getPassiveSkillMap();
    }

    @NotNull
    public PermissionMap getPermissionMap() {
        return safePlayerSession().getPermissionMap();
    }

    //endregion

    @NotNull
    public VariableList getVariableList() {
        return variableList;
    }

    @NotNull
    public ActionBarHandler getActionBar() {
        return actionBar;
    }

    @NotNull
    public Collection<PassiveSkill> isolateSkills(@NotNull TriggerMetadata triggerMetadata) {
        return triggerMetadata.getTriggerType().isActionHandSpecific() ? getPassiveSkillMap().isolateModifiers(triggerMetadata.getActionHand()) : getPassiveSkillMap().getModifiers();
    }

    public void triggerSkills(@NotNull TriggerType trigger) {
        triggerSkills(new TriggerMetadata(this, trigger));
    }

    public void triggerSkills(@NotNull TriggerMetadata triggerMetadata) {
        triggerSkills(triggerMetadata, isolateSkills(triggerMetadata));
    }

    public void triggerSkills(@NotNull TriggerMetadata triggerMetadata, @NotNull Iterable<PassiveSkill> skills) {
        this.triggerSkills(triggerMetadata, skills, true);
    }

    /**
     * Trigger a specific set of skills, with a specific context.
     *
     * @param triggerMetadata Context in which skills were triggered
     * @param skills          The list of skills being potentially triggered
     * @param flagCheck       Whether to check for WorldGuard flag. This allows for an optimization where
     *                        projectiles cache the value of the WG flag and only recompute it every 2 seconds.
     */
    public void triggerSkills(@NotNull TriggerMetadata triggerMetadata, @NotNull Iterable<PassiveSkill> skills, boolean flagCheck) {
        if (getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        if (flagCheck && MythicLib.plugin.getMMOConfig().flagCheckSkills && !MythicLib.plugin.getFlags().isFlagAllowed(getPlayer(), CustomFlag.MMO_ABILITIES)) return;

        for (var skill : skills) {
            final var handler = skill.getTriggeredSkill().getHandler();
            if (handler.isTriggerable() && skill.getTrigger().equals(triggerMetadata.getTriggerType()))
                skill.getTriggeredSkill().cast(triggerMetadata.toSkillMetadata(skill.getTriggeredSkill()));
        }
    }

    public void triggerSkills(@NotNull TriggerType trigger, @NotNull SkillMetadata skillMetadata) {
        if (getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        if (MythicLib.plugin.getMMOConfig().flagCheckSkills && !MythicLib.plugin.getFlags().isFlagAllowed(getPlayer(), CustomFlag.MMO_ABILITIES)) return;

        for (var skill : getPassiveSkillMap().getModifiers()) {
            final var handler = skill.getTriggeredSkill().getHandler();
            if (handler.isTriggerable() && skill.getTrigger().equals(trigger))
                skill.getTriggeredSkill().cast(skillMetadata);
        }
    }

    /**
     * Ticks every second in-game for every player with an active session
     */
    public void tickPlaying() {

        // Apply permanent potion effects
        getPermanentEffectMap().applyPermanentPotionEffects();
    }

    public void tickOnline() {

        // Warn about ghost sessions
        // [Safeguard]
        if (hasProfileSession() && getProfileSession().isGhost()) {
            MythicLib.plugin.getLogger().log(Level.SEVERE, "Ghost session detected for player " + getPlayerName() + " (" + getUniqueId() + "). Session dump: " + getProfileSession());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MMOPlayerData)) return false;

        MMOPlayerData that = (MMOPlayerData) o;
        return getUniqueId().equals(that.getUniqueId());
    }

    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }

    //region Static methods

    private static final Map<UUID, MMOPlayerData> PLAYER_DATA = new WeakHashMap<>();

    /**
     * Called everytime a player enters the server. If the
     * resource data is not initialized yet, initializes it.
     * <p>
     * This is called async using {@link AsyncPlayerPreLoginEvent} which does
     * not provide a Player instance, meaning the cached Player instance is NOT
     * loaded yet. It is only loaded when the player logs in using {@link PlayerJoinEvent}
     * <p>
     * This method is not guaranteed to be called only once per player, it might
     * be called once per MMO plugin every time the MMO plugins processes
     * the player join event.
     *
     * @param player Player whose data should be initialized
     */
    @NotNull
    public static MMOPlayerData setup(@NotNull Player player) {
        final var playerData = setup(player.getUniqueId());
        playerData.updatePlayer(player);
        return playerData;
    }

    @NotNull
    public static MMOPlayerData setup(@NotNull UUID uniqueId) {
        return PLAYER_DATA.computeIfAbsent(uniqueId, uuid -> new MMOPlayerData(false, uniqueId));
    }

    @NotNull
    public static MMOPlayerData get(@NotNull OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    @NotNull
    public static MMOPlayerData get(@NotNull UUID uuid) {
        return Objects.requireNonNull(PLAYER_DATA.get(uuid), "Player data not loaded");
    }

    @Nullable
    public static MMOPlayerData online(@NotNull Player player) {
        if (!player.isOnline()) return null;
        final MMOPlayerData found = PLAYER_DATA.get(player.getUniqueId());
        return found != null && found.isOnline() ? found : null;
    }

    /**
     * Use it at your own risk! Player data might not be loaded
     */
    @Nullable
    public static MMOPlayerData getOrNull(@NotNull Entity player) {
        return player instanceof Player ? getOrNull(player.getUniqueId()) : null;
    }

    /**
     * Use it at your own risk! Player data might not be loaded
     */
    @Nullable
    public static MMOPlayerData getOrNull(@NotNull UUID uuid) {
        return PLAYER_DATA.get(uuid);
    }

    /**
     * This is being used to easily check if an online player corresponds to
     * a real player or a Citizens NPC. Citizens NPCs do not have any player
     * data associated to them
     *
     * @return Checks if player data is loaded for a specific player
     */
    public static boolean has(@NotNull OfflinePlayer player) {
        return has(player.getUniqueId());
    }

    /**
     * This is being used to easily check if an online player corresponds to
     * a real player/profile or a Citizens NPC. Citizens NPCs do not have any player
     * data associated to them
     *
     * @return Checks if player data is loaded for a specific profile UUID
     */
    public static boolean has(@NotNull UUID uuid) {
        return PLAYER_DATA.containsKey(uuid);
    }

    /**
     * @return Currently loaded MMOPlayerData instances. This can be used to
     *         apply things like resource regeneration or other runnable based
     *         tasks instead of looping through online players and having to
     *         resort to a map-lookup-based get(Player) call
     */
    @NotNull
    public static Collection<MMOPlayerData> getLoaded() {
        return PLAYER_DATA.values();
    }

    /**
     * Calls some method for every player that has started playing
     * i.e that has chosen a profile and loaded his data.
     */
    public static void forEachPlaying(@NotNull Consumer<MMOPlayerData> action) {
        for (var registered : PLAYER_DATA.values())
            if (registered.isPlaying()) action.accept(registered);
    }

    public static void forEach(@NotNull Consumer<MMOPlayerData> action) {
        for (var registered : PLAYER_DATA.values()) action.accept(registered);
    }

    /**
     * Unloads all timed-out temporary player data. This must be
     * checked frequently to avoid player memory leaks.
     */
    public static void flushOfflinePlayerData() {
        PLAYER_DATA.values().removeIf(MMOPlayerData::isTimedOut);
    }

    //endregion

    //region Deprecated

    @Deprecated
    public <T> T getExternalData(String key, Class<T> objectType) {
        return null;
    }

    @Deprecated
    public void setExternalData(String key, Object obj) {
        // Nothing
    }

    @Deprecated
    public boolean hasExternalData(String key) {
        return false;
    }

    /**
     * @see #forEachPlaying(Consumer)
     * @deprecated
     */
    @Deprecated
    public static void forEachOnline(@NotNull Consumer<MMOPlayerData> action) {
        for (MMOPlayerData registered : PLAYER_DATA.values())
            if (registered.isOnline()) action.accept(registered);
    }

    @Deprecated
    public boolean hasStartedPlaying() {
        return isPlaying();
    }

    @Deprecated
    public boolean hasOfficialId() {
        return true;
    }

    @Deprecated
    public void setProfileId(@Nullable UUID profileId) {
        throw new IllegalStateException("Cannot change profile ID");
    }

    @Deprecated
    public boolean hasFullySynchronized() {
        return isPlaying();
    }

    @Deprecated
    public long getLastLogin() {
        return getLastLogActivity();
    }

    @Deprecated
    public static boolean isLoaded(UUID uuid) {
        return has(uuid);
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable Entity target) {
        Validate.isTrue(!triggerType.isActionHandSpecific(), "You must provide an action hand");
        triggerSkills(new TriggerMetadata(this, triggerType, target));
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @NotNull EquipmentSlot actionHand, @Nullable Entity target) {
        Validate.notNull(actionHand, "Action hand cannot be null");
        triggerSkills(new TriggerMetadata(this, triggerType, actionHand, null, target, null, null, null));
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable PlayerMetadata caster, @Nullable AttackMetadata attackMetadata, @Nullable Entity target) {
        final TriggerMetadata meta = new TriggerMetadata(this, triggerType, null, null, target, null, attackMetadata, caster);
        triggerSkills(meta);
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable PlayerMetadata caster, @Nullable Entity target, @Nullable AttackMetadata attackMetadata) {
        final Iterable<PassiveSkill> candidates = triggerType.isActionHandSpecific() ? getPassiveSkillMap().isolateModifiers(caster == null ? EquipmentSlot.MAIN_HAND : caster.getActionHand()) : getPassiveSkillMap().getModifiers();
        final TriggerMetadata meta = new TriggerMetadata(this, triggerType, null, null, target, null, attackMetadata, caster);
        triggerSkills(meta, candidates);
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable PlayerMetadata caster, @Nullable Entity target) {
        final Iterable<PassiveSkill> candidates = triggerType.isActionHandSpecific() ? getPassiveSkillMap().isolateModifiers(caster == null ? EquipmentSlot.MAIN_HAND : caster.getActionHand()) : getPassiveSkillMap().getModifiers();
        final TriggerMetadata meta = new TriggerMetadata(this, triggerType, null, null, target, null, null, caster);
        triggerSkills(meta, candidates);
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable PlayerMetadata caster, @NotNull Iterable<PassiveSkill> skills, @Nullable Entity target) {
        final TriggerMetadata meta = new TriggerMetadata(this, triggerType, null, null, target, null, null, caster);
        triggerSkills(meta, skills);
    }

    @Deprecated
    public void triggerSkills(@NotNull TriggerType triggerType, @Nullable PlayerMetadata caster, @NotNull Iterable<PassiveSkill> skills, @Nullable Entity target, @Nullable AttackMetadata attack) {
        final TriggerMetadata meta = new TriggerMetadata(this, triggerType, caster == null ? EquipmentSlot.MAIN_HAND : caster.getActionHand(), null, target, null, attack, caster);
        triggerSkills(meta, skills);
    }

    /**
     * @deprecated CooldownType now extends CooldownObject
     */
    @Deprecated
    public void applyCooldown(CooldownType cd, double value) {
        getCooldownMap().applyCooldown(cd.name(), value);
    }

    /**
     * @deprecated CooldownType now extends CooldownObject
     */
    @Deprecated
    public boolean isOnCooldown(CooldownType cd) {
        return getCooldownMap().isOnCooldown(cd.name());
    }

    @Deprecated
    public MMOPlayerData(@NotNull Player player) {
        this.lookup = false;
        this.entityId = Objects.requireNonNull(player, "Player cannot be null").getUniqueId();
        this.officialId = player.getUniqueId();
    }

    @Deprecated
    public MMOPlayerData(@NotNull UUID uniqueId) {
        this.lookup = true;
        this.entityId = Objects.requireNonNull(uniqueId, "UUID cannot be null");
        this.officialId = uniqueId;
    }

    //endregion
}

