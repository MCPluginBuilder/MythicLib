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
import java.util.function.Consumer;
import java.util.logging.Level;

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
    private PlayerSessionState state = PlayerSessionState.INITIALIZED;

    @Nullable
    private Consumer<PlayerSession> onReadyCallback;

    /**
     * When logging in, MythicLib waits for all MMO plugins
     * to load their data before toggling on readiness flag
     * of this session object
     */
    private List<NamespacedKey> waiting;
    private List<NamespacedKey> waitingSnapshot;

    public PlayerSession(@NotNull MMOPlayerData playerData) {
        this.playerData = playerData;

        MythicLib.plugin.getLogger().log(Level.INFO, "====================== CREATING SESSION " + this.toString());
    }

    @NotNull
    public UUID getProfileId() {
        return Objects.requireNonNull(profileId, "No profile chosen");
    }

    public boolean hasProfile() {
        return this.profileId != null;
    }

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
        if (state == PlayerSessionState.INITIALIZED) return false;

        return !this.waiting.contains(Objects.requireNonNull(key, "Key cannot be null"));
    }

    public void startOpening(@Nullable UUID profileId,
                             @Nullable NamespacedKey profilePluginNsk,
                             @NotNull List<NamespacedKey> modules,
                             @Nullable Consumer<PlayerSession> onReadyCallback) {
        Validate.isTrue(this.state == PlayerSessionState.INITIALIZED, "Session already initialized");

        this.state = PlayerSessionState.OPENING;
        this.waiting = Objects.requireNonNull(modules, "Modules cannot be null");
        this.waitingSnapshot = new ArrayList<>(modules);
        this.onReadyCallback = onReadyCallback;
        this.profileId = profileId;

        Bukkit.broadcastMessage("========== START OPENING SESSION " + this.toString());

        checkReadiness(); // In case there are no plugins
    }

    public void markAsReady(@NotNull NamespacedKey key) {
        Validate.isTrue(playerData.isOnline() || playerData.isLookup(), "Player went offline");
        Validate.isTrue(state == PlayerSessionState.OPENING, "Profile session not opening (in state " + this.state.name() + ")");

        final var found = this.waiting.remove(key);
        Validate.isTrue(found, String.format("Module %s already synced", key));

        Bukkit.broadcastMessage("data loaded, marking as rdy plugin " + key);

        checkReadiness(); // Check if all plugins have loaded their data
    }

    private void checkReadiness() {

        // Wait for all plugins to load their data
        if (!this.waiting.isEmpty()) return;

        ////////////////////////////////
        // Player can start playing
        ////////////////////////////////
        this.state = PlayerSessionState.OPEN;
        this.playerData.startPlaying();
        if (this.onReadyCallback != null) this.onReadyCallback.accept(this);

        Bukkit.broadcastMessage("========= START PLAYING " + this.toString());
    }

    public void startClosing(@Nullable Consumer<PlayerSession> onReadyCallback) {
        Validate.isTrue(state == PlayerSessionState.OPEN, "Cannot close a non ready session");

        this.state = PlayerSessionState.CLOSING;
        this.waiting = waitingSnapshot;
        this.onReadyCallback = onReadyCallback;

        Bukkit.broadcastMessage("========== START CLOSING SESSION " + toString());

        checkClosed(); // In case there are no plugins
    }

    public void setCallback(@NotNull Consumer<PlayerSession> onReadyCallback) {
        Validate.isTrue(this.onReadyCallback == null, "Callback already set");
        this.onReadyCallback = onReadyCallback;
    }

    public void markAsClosed(@NotNull NamespacedKey key) {
        Validate.isTrue(state == PlayerSessionState.CLOSING, "Session is not closing (in state " + this.state.name() + ")");

        final var found = this.waiting.remove(key);
        Validate.isTrue(found, String.format("Module %s already marked as closed", key));

        Bukkit.broadcastMessage("Module " + key + " marked as closed :: " + this.toString());

        checkClosed(); // Check if all plugins have saved their data
    }

    private void checkClosed() {

        // Wait for all plugins to store their data
        if (!this.waiting.isEmpty()) return;

        ////////////////////////////////
        // Session invalidated
        ////////////////////////////////
        this.state = PlayerSessionState.DEAD;
        this.playerData.initialiazeNextProfileSession();
        if (this.onReadyCallback != null) this.onReadyCallback.accept(this);

        Bukkit.broadcastMessage("========= SESSION INVALIDATED " + this.toString());
    }

    @Override
    public String toString() {
        return "PlayerSession{" + "user=" + this.playerData.getUniqueId() + ", profileId=" + profileId + ", state=" + state + ", waiting=" + waiting + '}';
    }
}
