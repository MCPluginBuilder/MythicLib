package io.lumine.mythic.lib.profile;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
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
public class PlayerSession {
    private final MMOPlayerData playerData;

    /**
     * UUID of profile chosen
     */
    @Nullable
    private UUID profileId;

    /**
     * State of player session.
     */
    private PlayerSessionState state = PlayerSessionState.INIT;

    @NotNull
    private final List<PlayerSessionCallback> callbacks = new ArrayList<>();

    /**
     * When logging in, MythicLib waits for all MMO plugins
     * to load their data before toggling on readiness flag
     * of this session object
     */
    private List<NamespacedKey> waiting;

    public PlayerSession(@NotNull MMOPlayerData playerData) {
        this.playerData = playerData;
    }

    @NotNull
    public UUID getProfileId() {
        return Objects.requireNonNull(profileId, "No profile chosen");
    }

    public boolean hasProfile() {
        return this.profileId != null;
    }

    /**
     * @return If the player has started playing
     * @see MMOPlayerData#hasStartedPlaying()
     */
    public boolean isReady() {
        return state == PlayerSessionState.OPEN || state == PlayerSessionState.CLOSING;
    }

    public boolean isClosing() {
        return state == PlayerSessionState.CLOSING;
    }

    public boolean isDead() {
        return state == PlayerSessionState.DEAD;
    }

    public boolean isReady(@NotNull NamespacedKey key) {

        // If session is globally ready, all plugins loaded their data
        // If session is closing, means it was ready before.
        if (state == PlayerSessionState.OPEN || state == PlayerSessionState.CLOSING) return true;

        // If session is not opened yet, no way it's ready
        if (state == PlayerSessionState.INIT) return false;

        return !this.waiting.contains(Objects.requireNonNull(key, "Key cannot be null"));
    }

    public void startOpening() {
        if (this.state != PlayerSessionState.INIT) return;

        Bukkit.broadcastMessage("========== START OPENING SESSION " + this.toString());

        this.state = PlayerSessionState.OPENING;
        this.waiting = MythicLib.plugin.getProfileHandler().collectModules();
        this.callbacks.clear();
    }

    public void addCallback(@NotNull PlayerSessionCallback callback) {
        Validate.notNull(callback, "Callback cannot be null");
        Validate.isTrue(this.state == PlayerSessionState.OPENING || this.state == PlayerSessionState.CLOSING, "Cannot add callback to a non opening/closing session");

        this.callbacks.add(callback);
    }

    public void chooseProfile(@Nullable UUID profileId) {
        Validate.isTrue(this.profileId == null, "Profile already selected");

        startOpening();
        this.profileId = profileId;
    }

    public synchronized void markAsReady(@NotNull NamespacedKey key) {
        Validate.isTrue(playerData.isOnline() || playerData.isLookup(), "Player went offline");
        startOpening();
        Validate.isTrue(state == PlayerSessionState.OPENING, "Profile session not opening (in state " + this.state.name() + ")");

        final var found = this.waiting.remove(key);
        Validate.isTrue(found, String.format("Module %s already synced", key));

        Bukkit.broadcastMessage("data loaded, marking as rdy module " + key);

        checkReadiness(); // Check if all plugins have loaded their data
    }

    private void checkReadiness() {

        // Wait for all plugins to load their data
        if (!this.waiting.isEmpty()) return;

        Bukkit.broadcastMessage("========= START PLAYING " + this.toString());

        ////////////////////////////////
        // Player can start playing
        ////////////////////////////////
        this.state = PlayerSessionState.OPEN;
        this.playerData.startPlaying();
        this.callbacks.forEach(callback -> callback.callback(this));
    }

    public void startClosing() {
        Validate.isTrue(state == PlayerSessionState.OPEN, "Cannot close a non ready session");

        Bukkit.broadcastMessage("========== START CLOSING SESSION " + toString());

        this.state = PlayerSessionState.CLOSING;
        this.playerData.clearTemporaryHandlers();
        this.waiting = MythicLib.plugin.getProfileHandler().collectModules();
        this.callbacks.clear();

        checkClosed(); // In case there are no plugins
    }

    public synchronized void markAsClosed(@NotNull NamespacedKey key) {
        Validate.isTrue(state == PlayerSessionState.CLOSING, "Session is not closing (in state " + this.state.name() + ")");

        final var found = this.waiting.remove(key);
        Validate.isTrue(found, String.format("Module %s already marked as closed", key));

        Bukkit.broadcastMessage("Module " + key + " marked as closed :: " + this.toString());

        checkClosed(); // Check if all plugins have saved their data
    }

    private void checkClosed() {

        // Wait for all plugins to store their data
        if (!this.waiting.isEmpty()) return;

        Bukkit.broadcastMessage("========= SESSION INVALIDATED " + this.toString());

        ////////////////////////////////
        // Session invalidated
        ////////////////////////////////
        Bukkit.getScheduler().runTask(MythicLib.plugin, () -> {
            this.state = PlayerSessionState.DEAD;
            this.playerData.initialiazeNextProfileSession();
            this.callbacks.forEach(callback -> callback.callback(this));
        });
    }

    @Override
    public String toString() {
        return "PlayerSession{" + "user=" + this.playerData.getUniqueId() + ", profileId=" + profileId + ", state=" + state + ", waiting=" + waiting + '}';
    }
}
