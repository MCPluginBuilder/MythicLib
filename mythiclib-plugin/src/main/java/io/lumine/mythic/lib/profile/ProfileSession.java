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

    private final List<ProfileSessionCallback> callbacks = new ArrayList<>();

    //endregion

    @NotNull
    private ProfileSessionState getAndSetState(@NotNull ProfileSessionState newState) {
        // This method does not take the lock
        final var oldState = this.state;
        this.state = Objects.requireNonNull(newState, "New state cannot be null");
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

    public void callSessionUpdateEvent(ProfileSessionState oldState) {
        // This method does not take the lock
        UtilityMethods.debug(MythicLib.plugin, "Session", profileId + ": " + oldState.name() + " -> " + this.state.name());
        Bukkit.getPluginManager().callEvent(new SessionUpdateEvent(playerData, this, oldState, this.state));
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

    public void reset() {
        final ProfileSessionState oldState;
        synchronized (this.fsmLock) {
            Validate.isTrue(state.isDead(), "Can only reset session in state DEAD");
            oldState = getAndSetState(ProfileSessionState.CREATED);
        }

        callSessionUpdateEvent(oldState);
    }

    private void initializeOpening() {
        final ProfileSessionState oldState;
        synchronized (this.fsmLock) {
            if (this.state != ProfileSessionState.CREATED) return;

            oldState = getAndSetState(ProfileSessionState.OPENING);
            this.waiting = MythicLib.plugin.getProfileHandler().collectModules();
            this.loaded = new ArrayList<>();
            this.callbacks.clear();
        }

        callSessionUpdateEvent(oldState);
    }

    public void markAsReady(@NotNull NamespacedKey key) {
        Validate.notNull(key, "Module key cannot be null");
        initializeOpening();

        synchronized (this.fsmLock) {
            Validate.isTrue(state == ProfileSessionState.OPENING, "Session is not opening (in state " + this.state.name() + ")");
            final var hasBeenOpen = this.waiting.remove(key);
            Validate.isTrue(hasBeenOpen, String.format("Module %s already synced", key));
            this.loaded.add(key);
        }

        // Check for full session open
        checkReadiness();
    }

    public void addOpenCallback(@NotNull ProfileSessionCallback callback) {
        Validate.notNull(callback, "Callback cannot be null");
        initializeOpening();

        synchronized (this.fsmLock) {
            Validate.isTrue(this.state == ProfileSessionState.OPENING, "Session is not opening (in state " + this.state.name() + ")");
            this.callbacks.add(callback);
        }
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
            this.openDataSession();
        }

        callbacks.forEach(callback -> callback.callback(this));
        callSessionUpdateEvent(oldState);
    }

    public void initializeClosing() {

        final ProfileSessionState oldState;
        final boolean checkClosed;
        synchronized (this.fsmLock) {
            if (state.isClosing() || state.isDead()) return;

            // Abort opening
            if (state == ProfileSessionState.CREATED || state == ProfileSessionState.OPENING) {
                oldState = getAndSetState(ProfileSessionState.ABORT);
                this.callbacks.clear();
                this.playerData.clearTemporaryHandlers();
                this.waiting = this.loaded;
                checkClosed = false;
            }

            // Close normally
            else if (state == ProfileSessionState.OPEN) {
                oldState = getAndSetState(ProfileSessionState.CLOSING);
                this.callbacks.clear();
                this.playerData.clearTemporaryHandlers();
                this.waiting = this.loaded;
                this.closeDataSession();
                checkClosed = true;
            }

            // Wth
            else throw new IllegalStateException("Unhandled session state " + this.state.name());
        }

        callSessionUpdateEvent(oldState);

        if (checkClosed) checkClosed();
    }

    public void addCloseCallback(@NotNull ProfileSessionCallback callback) {
        Validate.notNull(callback, "Callback cannot be null");
        initializeClosing();

        synchronized (this.fsmLock) {
            Validate.isTrue(this.state.isClosing(), "Session is not closing");
            this.callbacks.add(callback);
        }
    }

    public void markAsClosed(@NotNull NamespacedKey key) {
        Validate.notNull(key, "Module key cannot be null");
        initializeClosing();

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
            oldState = getAndSetState(state == ProfileSessionState.ABORT ? ProfileSessionState.DEAD_EARLY : ProfileSessionState.DEAD);
            this.playerData.saveCurrentProfileSession();
        }

        callbacks.forEach(callback -> callback.callback(this));
        callSessionUpdateEvent(oldState);
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
    private final CooldownMap cooldownMap = new CooldownMap();
    private final VariableList variableList = new VariableList(VariableScope.PROFILE);

    @NotNull
    public StatMap getStatMap() {
        return statMap;
    }

    @NotNull
    public CooldownMap getCooldownMap() {
        return cooldownMap;
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
    public VariableList getVariableList() {
        return variableList;
    }

    private void openDataSession() {
        statMap.openSession();
        cooldownMap.openSession();
        skillModifierMap.openSession();
        permEffectMap.openSession();
        particleEffectMap.openSession();
        passiveSkillMap.openSession();
        permissionMap.openSession();
        //private final VariableList variableList = new VariableList(VariableScope.PLAYER);
    }

    private void closeDataSession() {
        statMap.closeSession();
        cooldownMap.closeSession();
        skillModifierMap.closeSession();
        permEffectMap.closeSession();
        particleEffectMap.closeSession();
        passiveSkillMap.closeSession();
        permissionMap.closeSession();
        //private final VariableList variableList = new VariableList(VariableScope.PLAYER);
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
