package io.lumine.mythic.lib.profile;

import io.lumine.mythic.lib.MythicLib;
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
     * State of player session.
     */
    private ProfileSessionState state = ProfileSessionState.CREATED;

    @NotNull
    private final List<ProfileSessionCallback> callbacks = new ArrayList<>();

    /**
     * When logging in, MythicLib waits for all MMO plugins
     * to load their data before toggling on readiness flag
     * of this session object
     */
    private List<NamespacedKey> waiting;

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

    /**
     * @return If the player is currently playing. This will return true,
     *         as soon as the player logs out or switches profile.
     * @see MMOPlayerData#isPlaying()
     */
    public boolean isReady() {
        return state == ProfileSessionState.OPEN;
    }

    public boolean isDead() {
        return state == ProfileSessionState.DEAD;
    }

    public synchronized boolean isReady(@NotNull NamespacedKey key) {

        // If session is globally ready, all plugins loaded their data
        // If session is closing, means it was ready before.
        if (state == ProfileSessionState.OPEN || state == ProfileSessionState.CLOSING) return true;

        // If session is not opened yet, no way it's ready
        if (state == ProfileSessionState.CREATED) return false;

        return !this.waiting.contains(Objects.requireNonNull(key, "Key cannot be null"));
    }

    private void initialize() {
        if (this.state != ProfileSessionState.CREATED && this.state != ProfileSessionState.DEAD) return;

        this.state = ProfileSessionState.OPENING;
        this.waiting = MythicLib.plugin.getProfileHandler().collectModules();
        this.callbacks.clear();
    }

    public synchronized void addOpenCallback(@NotNull ProfileSessionCallback callback) {
        Validate.notNull(callback, "Callback cannot be null");
        initialize();
        Validate.isTrue(this.state == ProfileSessionState.OPENING, "Session is not opening");

        this.callbacks.add(callback);
    }

    public synchronized void markAsReady(@NotNull NamespacedKey key) {
        // TODO move online check elsewhere
        Validate.isTrue(playerData.isOnline() || playerData.isLookup(), "Player went offline");
        initialize();
        Validate.isTrue(state == ProfileSessionState.OPENING, "Profile session not opening (in state " + this.state.name() + ")");

        final var found = this.waiting.remove(key);
        Validate.isTrue(found, String.format("Module %s already synced", key));

        checkReadiness(); // Check if all plugins have loaded their data
    }

    private void checkReadiness() {

        // Wait for all plugins to load their data
        if (!this.waiting.isEmpty()) return;

        ////////////////////////////////
        // Session opened
        ////////////////////////////////

        this.state = ProfileSessionState.OPEN;
        //this.playerData.startPlaying();
        this.callbacks.forEach(callback -> callback.callback(this));
        this.openDataSession();
        this.dataSession.markActive();
    }

    private void abortOpening() {
        this.state = ProfileSessionState.DEAD;
        this.playerData.clearTemporaryHandlers();
        this.waiting = null;
        this.callbacks.clear();
        this.incrementDataSession();
    }

    public synchronized void startClosing() {

        // Abort opening
        if (state == ProfileSessionState.CREATED || state == ProfileSessionState.OPENING) {
            abortOpening();
        }

        // Open
        else if (state == ProfileSessionState.OPEN) {

            this.state = ProfileSessionState.CLOSING;
            this.playerData.clearTemporaryHandlers();
            this.waiting = MythicLib.plugin.getProfileHandler().collectModules();
            this.callbacks.clear();
            this.closeDataSession();
            incrementDataSession();

            checkClosed();
        }

        // Already closing
        else if (state != ProfileSessionState.CLOSING) {
            Validate.isTrue(state == ProfileSessionState.DEAD, "Cannot close a dead session");
        }
    }

    public synchronized void addCloseCallback(@NotNull ProfileSessionCallback callback) {
        Validate.notNull(callback, "Callback cannot be null");
        startClosing();
        Validate.isTrue(this.state == ProfileSessionState.CLOSING, "Session is not closing");

        this.callbacks.add(callback);
    }

    public synchronized void markAsClosed(@NotNull NamespacedKey key) {
        startClosing();
        Validate.isTrue(state == ProfileSessionState.CLOSING, "Session is not closing (in state " + this.state.name() + ")");

        final var found = this.waiting.remove(key);
        Validate.isTrue(found, String.format("Module %s already marked as closed", key));

        checkClosed(); // Check if all plugins have saved their data
    }

    private void checkClosed() {

        // Wait for all plugins to store their data
        if (!this.waiting.isEmpty()) return;

        ////////////////////////////////
        // Session closed
        ////////////////////////////////

        this.setLastActivity();
        this.state = ProfileSessionState.DEAD;
        this.playerData.saveCurrentProfileSession();
        this.playerData.updatePlayer(null);
        this.callbacks.forEach(callback -> callback.callback(this));
    }

    @Override
    public String toString() {
        return "PlayerSession{" + "user=" + this.playerData.getUniqueId() + ", profileId=" + profileId + ", state=" + state + ", waiting=" + waiting + '}';
    }

    //region Player data session

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

    @NotNull
    private DataSession dataSession = new DataSession(this);

    @NotNull
    public DataSession getDataSession() {
        return dataSession;
    }

    private void incrementDataSession() {
        this.dataSession.markDead();
        this.dataSession = new DataSession(this);
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
