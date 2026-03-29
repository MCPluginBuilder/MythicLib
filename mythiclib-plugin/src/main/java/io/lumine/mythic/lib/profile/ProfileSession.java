package io.lumine.mythic.lib.profile;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.session.SessionUpdateEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.player.cooldown.CooldownMap;
import io.lumine.mythic.lib.player.particle.ParticleEffectMap;
import io.lumine.mythic.lib.player.permission.PermissionMap;
import io.lumine.mythic.lib.player.potion.PermanentPotionEffectMap;
import io.lumine.mythic.lib.player.skill.PassiveSkillMap;
import io.lumine.mythic.lib.player.skillmod.SkillModifierMap;
import io.lumine.mythic.lib.rpg.provided.PlayerResourceData;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * One session object per player profile switch. When the player
 * chooses the profile they are playing on, a session object is
 * created.
 * <p>
 * If there is no profile plugin installed, the profile session
 * object simply contains the official Mojang player UUID.
 *
 * @author jules
 */
public class ProfileSession {
    private final MMOPlayerData playerData;

    /**
     * UUID of profile chosen, or null if none
     */
    @Nullable
    private final UUID profileId;

    /**
     * @param parent    Session parent player data
     * @param profileId ID of profile chosen, or official player ID if none
     */
    public ProfileSession(@NotNull MMOPlayerData parent, @Nullable UUID profileId) {
        this.playerData = parent;
        this.profileId = profileId;

        this.statMap = new StatMap(parent);
        this.skillModifierMap = new SkillModifierMap(parent);
        this.permEffectMap = new PermanentPotionEffectMap(parent);
        this.particleEffectMap = new ParticleEffectMap(parent);
        this.passiveSkillMap = new PassiveSkillMap(parent);
        this.permissionMap = new PermissionMap(parent);
        this.cooldownMap = new CooldownMap();
        this.resourceData = new PlayerResourceData(parent);
        this.variableList = new VariableList(VariableScope.PROFILE);
    }

    /**
     * Recycle a session object when switching profile, allowing to
     * keep the same data maps while freezing session states to avoid
     * inconsistent states due to profile switches.
     *
     * @param parent   Session parent player data
     * @param recycled Session to recycle, should be the previous session of the same player data
     */
    public ProfileSession(@NotNull MMOPlayerData parent, ProfileSession recycled) {
        this.playerData = parent;
        this.profileId = recycled.profileId;

        this.statMap = recycled.statMap;
        this.skillModifierMap = recycled.skillModifierMap;
        this.permEffectMap = recycled.permEffectMap;
        this.particleEffectMap = recycled.particleEffectMap;
        this.passiveSkillMap = recycled.passiveSkillMap;
        this.permissionMap = recycled.permissionMap;
        this.cooldownMap = recycled.cooldownMap;
        this.resourceData = recycled.resourceData;
        this.variableList = recycled.variableList;
    }

    public boolean hasProfile() {
        return profileId != null;
    }

    @NotNull
    public UUID getProfileId() {
        return Objects.requireNonNull(profileId, "No profile");
    }

    //region FSM

    //region Internal FSM State

    private volatile ProfileSessionState state = ProfileSessionState.CREATED;

    private final Object fsmLock = new Object();

    /**
     * When logging in, MythicLib waits for all MMO plugins
     * to load their data before toggling on readiness flag
     * of this session object.
     * <p>
     * In case the opening of a session is aborted, the list
     * of loaded modules is dumped
     */
    private List<NamespacedKey> waiting, loaded;

    /**
     * Last update reason when switching to opening or closing states
     */
    private SessionUpdateReason lastUpdateReason;
    private long lastStateUpdateTimestamp;

    private final List<ProfileSessionCallback> callbacks = new ArrayList<>();

    //endregion

    private static final long GHOST_THRESHOLD_MILLIS = 1000 * 10;

    private static final List<NamespacedKey> GHOST_CHECK_BLACKLIST = List.of(
            new NamespacedKey("mmocore", "force_class_select")
    );

    public boolean isGhost() {
        if (!this.state.isWaiting()) return false;
        if (System.currentTimeMillis() < this.lastStateUpdateTimestamp + GHOST_THRESHOLD_MILLIS) return false;
        for (var key : this.waiting) if (!GHOST_CHECK_BLACKLIST.contains(key)) return true;
        return false;
    }

    @NotNull
    private ProfileSessionState getAndSetState(@NotNull ProfileSessionState newState) {
        // This method does not take the lock
        final var oldState = this.state;
        this.state = Objects.requireNonNull(newState, "New state cannot be null");
        this.lastStateUpdateTimestamp = System.currentTimeMillis();
        return oldState;
    }

    @NotNull
    public ProfileSessionState getState() {
        return state;
    }

    /**
     * @return If the player is currently playing. This will return true,
     *         as soon as the player logs out or switches profile.
     * @see MMOPlayerData#isPlaying()
     */
    public boolean isReady() {
        return state == ProfileSessionState.OPEN;
    }

    public boolean isDead() {
        return state.isDead();
    }

    public boolean wasReady() {
        return state.wasReady();
    }

    public void callSessionUpdateEvent(@NotNull ProfileSessionState oldState, @NotNull SessionUpdateReason reason) {
        Validate.notNull(reason, "Reason cannot be null");

        // This method does not take the lock
        UtilityMethods.debug(MythicLib.plugin, "Session", this.playerData.getPlayerName() + " (" + profileId + "): " + oldState.name() + " -> " + this.state.name());
        Bukkit.getPluginManager().callEvent(new SessionUpdateEvent(playerData, this, reason, oldState, this.state));
    }

    public boolean isReady(@NotNull NamespacedKey key) {
        synchronized (this.fsmLock) {

            // If session is globally ready, all plugins loaded their data
            // If session is closing, means it was ready before.
            if (state == ProfileSessionState.OPEN || state == ProfileSessionState.CLOSING) return true;

            // If session is not opened yet, no way it's ready
            if (state == ProfileSessionState.CREATED) return false;

            return this.loaded.contains(key);
        }
    }

    public void initializeSession(@NotNull SessionUpdateReason reason) {
        synchronized (this.fsmLock) {
            Validate.isTrue(state == ProfileSessionState.CREATED, "Can only initialize new session from state DEAD");
        }

        callSessionUpdateEvent(ProfileSessionState.DEAD, reason);
        initializeOpening(reason);
    }

    private void initializeOpening(@NotNull SessionUpdateReason reason) {
        Validate.notNull(reason, "Reason cannot be null");

        final ProfileSessionState oldState;
        synchronized (this.fsmLock) {
            Validate.isTrue(state == ProfileSessionState.CREATED, "Can only initialize opening from state CREATED");

            this.lastUpdateReason = reason;
            oldState = getAndSetState(ProfileSessionState.OPENING);
            this.waiting = MythicLib.plugin.getProfileHandler().collectModules();
            this.loaded = new ArrayList<>();
        }

        callSessionUpdateEvent(oldState, reason);

        checkReadiness();
    }

    public void markAsReady(@NotNull NamespacedKey key) {
        Validate.notNull(key, "Module key cannot be null");

        synchronized (this.fsmLock) {
            Validate.isTrue(state == ProfileSessionState.OPENING, "Session is not opening (in state " + this.state.name() + ")");
            final var hasBeenOpen = this.waiting.remove(key);
            Validate.isTrue(hasBeenOpen, String.format("Module %s already synced", key));
            this.loaded.add(key);
        }

        // Check for full session open
        checkReadiness();
    }

    private void checkReadiness() {

        final ProfileSessionState oldState;
        synchronized (this.fsmLock) {

            // Wait for all plugins to load their data
            if (!this.waiting.isEmpty()) return;

            ////////////////////////////////
            // Session opened
            ////////////////////////////////

            oldState = getAndSetState(ProfileSessionState.OPEN);
        }

        callSessionUpdateEvent(oldState, lastUpdateReason);
        // Only open data session (== update stats and attributes)
        // after broadcasting the Bukkit event (== plugin callbacks)
        this.openDataSession();
        this.lastUpdateReason = null;
    }

    public void shutdown() {
        try {
            initializeClosing(SessionUpdateReason.LOG_OUT);
        } catch (Exception ignored) {
            // Nothing
        }
    }

    public void initializeClosing(@NotNull SessionUpdateReason reason) {
        Validate.notNull(reason, "Reason cannot be null");

        final ProfileSessionState oldState;
        synchronized (this.fsmLock) {
            if (state.isClosing() || state.isDead()) return;

            // Abort opening
            if (state == ProfileSessionState.CREATED || state == ProfileSessionState.OPENING) {
                oldState = getAndSetState(ProfileSessionState.ABORTING);
            }

            // Close normally
            else if (state == ProfileSessionState.OPEN) {
                oldState = getAndSetState(ProfileSessionState.CLOSING);
                this.closeDataSession();
            }

            // Wth
            else throw new IllegalStateException("Unhandled session state " + this.state.name());

            this.lastUpdateReason = reason;
            this.callbacks.clear();
            this.playerData.clearTemporaryHandlers();
            this.waiting = this.loaded;
        }

        callSessionUpdateEvent(oldState, reason);

        checkClosed();
    }

    public void addCloseCallback(@NotNull ProfileSessionCallback callback) {
        Validate.notNull(callback, "Callback cannot be null");

        synchronized (this.fsmLock) {
            Validate.isTrue(this.state.isClosing(), "Session is not closing");
            this.callbacks.add(callback);
        }
    }

    public void markAsClosed(@NotNull NamespacedKey key) {
        Validate.notNull(key, "Module key cannot be null");

        // Mark module as closed
        final boolean hasBeenClosed;
        synchronized (this.fsmLock) {
            Validate.isTrue(state.isClosing(), "Session is not closing (in state " + this.state.name() + ")");
            hasBeenClosed = this.waiting.remove(key);
        }
        Validate.isTrue(hasBeenClosed, String.format("Module %s already marked as closed", key));

        // Check for full session close
        checkClosed();
    }

    private void checkClosed() {

        final ProfileSessionState oldState;
        synchronized (this.fsmLock) {

            // Wait for all plugins to store their data
            if (!this.waiting.isEmpty()) return;

            ////////////////////////////////
            // Session closed
            ////////////////////////////////

            this.setLastActivity();
            oldState = getAndSetState(state == ProfileSessionState.ABORTING ? ProfileSessionState.DEAD_EARLY : ProfileSessionState.DEAD);
        }

        this.playerData.saveCurrentProfileSession();
        callbacks.forEach(callback -> callback.callback(this));
        callSessionUpdateEvent(oldState, lastUpdateReason);
        this.lastUpdateReason = null;
        this.playerData.applyNextSessionBuffer();
    }

    //endregion

    @Override
    public String toString() {
        return "PlayerSession{" + "user=" + this.playerData.getUniqueId() + ", profileId=" + profileId + ", state=" + state + ", waiting=" + waiting + '}';
    }

    //region Player data

    private final StatMap statMap;
    private final SkillModifierMap skillModifierMap;
    private final PermanentPotionEffectMap permEffectMap;
    private final ParticleEffectMap particleEffectMap;
    private final PassiveSkillMap passiveSkillMap;
    private final PermissionMap permissionMap;
    private final CooldownMap cooldownMap;
    private final PlayerResourceData resourceData;
    private final VariableList variableList;

    @NotNull
    public StatMap getStatMap() {
        return statMap;
    }

    @NotNull
    public SkillModifierMap getSkillModifierMap() {
        return skillModifierMap;
    }

    @NotNull
    public PermanentPotionEffectMap getPermanentEffectMap() {
        return permEffectMap;
    }

    @NotNull
    public ParticleEffectMap getParticleEffectMap() {
        return particleEffectMap;
    }

    @NotNull
    public PassiveSkillMap getPassiveSkillMap() {
        return passiveSkillMap;
    }

    @NotNull
    public PermissionMap getPermissionMap() {
        return permissionMap;
    }

    @NotNull
    public CooldownMap getCooldownMap() {
        return cooldownMap;
    }

    @NotNull
    public PlayerResourceData getResources() {
        return resourceData;
    }

    @NotNull
    public VariableList getVariableList() {
        return variableList;
    }

    private void openDataSession() {
        statMap.openSession();
        skillModifierMap.openSession();
        permEffectMap.openSession();
        particleEffectMap.openSession();
        passiveSkillMap.openSession();
        permissionMap.openSession();
        cooldownMap.openSession();
        resourceData.openSession();
        // variableList: nothing needed
    }

    private void closeDataSession() {
        statMap.closeSession();
        skillModifierMap.closeSession();
        permEffectMap.closeSession();
        particleEffectMap.closeSession();
        passiveSkillMap.closeSession();
        permissionMap.closeSession();
        cooldownMap.closeSession();
        resourceData.closeSession();
        // variableList: nothing needed
    }

    //endregion

    //region Activity and timeout

    private long lastActivity;

    private void setLastActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    /**
     * When a player logs off, MythicLib player data is temporarily saved
     * for 24 hours before it is eventually flushed from memory.
     */
    private static final long TIME_OUT_MILLIS = 1000 * 60 * 60 * 24;

    public boolean isTimedOut() {
        return isDead() && lastActivity + TIME_OUT_MILLIS < System.currentTimeMillis();
    }

    //endregion
}
