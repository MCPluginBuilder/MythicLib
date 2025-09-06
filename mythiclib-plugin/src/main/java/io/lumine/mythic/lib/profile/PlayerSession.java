package io.lumine.mythic.lib.profile;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.MMOPlugin;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
    private PlayerSessionState state = PlayerSessionState.OPENING;

    /**
     * When logging in, MythicLib waits for all MMO plugins
     * to load their data before toggling on readiness flag
     * of this session object
     */
    @NotNull
    private final List<String> waiting = new LinkedList<>();

    public PlayerSession(@NotNull MMOPlayerData playerData) {
        this.playerData = playerData;
        initializeWaitingList();

        MythicLib.plugin.getLogger().log(Level.INFO, "====================== CREATING SESSION " + this.toString());
    }

    private void initializeWaitingList() {
        for (var mmoPlugin : MythicLib.plugin.getMMOPlugins())
            if (mmoPlugin.hasData()) this.waiting.add(mmoPlugin.getName());
    }

    @NotNull
    public UUID getProfileId() {
        return Objects.requireNonNull(profileId, "No profile chosen");
    }

    public void applyProfileId(@NotNull UUID profileId) {
        Validate.isTrue(this.profileId == null, "Profile already applied");

        this.profileId = Objects.requireNonNull(profileId, "Profile ID cannot be null");
    }

    public boolean hasProfile() {
        return this.profileId != null;
    }

    public boolean isDead() {
        return state == PlayerSessionState.DEAD;
    }

    public boolean isAlive() {
        return state != PlayerSessionState.DEAD;
    }

    public boolean isReady() {
        return state == PlayerSessionState.READY || state == PlayerSessionState.CLOSING;
    }

    public boolean isClosing() {
        return state == PlayerSessionState.CLOSING;
    }

    public boolean isReady(@NotNull MMOPlugin mmoPlugin) {

        // If session is globally ready, all plugins loaded their data
        // If session is closing, means it was ready before.
        if (state == PlayerSessionState.READY || state == PlayerSessionState.CLOSING) return true;

        return !this.waiting.contains(Objects.requireNonNull(mmoPlugin, "Plugin cannot be null").getName());
    }

    public void startClosing() {
        Validate.isTrue(state == PlayerSessionState.READY, "Cannot close a non ready session");

        Bukkit.broadcastMessage("========== START CLOSING SESSION");

        this.state = PlayerSessionState.CLOSING;
        initializeWaitingList();
    }

    public void markAsClosed(@NotNull MMOPlugin mmoPlugin) {
        Validate.isTrue(mmoPlugin.hasData(), "This plugin has no data");
        Validate.isTrue(state == PlayerSessionState.CLOSING, "Session is not closing (in state " + this.state.name() + ")");

        final var found = this.waiting.remove(mmoPlugin.getName());
        Validate.isTrue(found, String.format("Plugin data %s already marked as closed", mmoPlugin.getName()));

        Bukkit.broadcastMessage("Marked as closed plugin " + mmoPlugin.getName() + " " + this.toString());

        // Wait for all plugins to load their data
        if (!this.waiting.isEmpty()) return;

        ////////////////////////////////
        // Session invalidated
        ////////////////////////////////
        this.state = PlayerSessionState.DEAD;
        this.playerData.initialiazeNextProfileSession();
        Bukkit.broadcastMessage("========= SESSION INVALIDATED " + this.toString());
    }

    public void markAsReady(@NotNull MMOPlugin mmoPlugin) {
        Validate.isTrue(mmoPlugin.hasData(), "This plugin has no data");
        Validate.isTrue(playerData.isOnline() || playerData.isLookup(), "Player went offline");
        Validate.isTrue(state == PlayerSessionState.OPENING, "Profile session already ready");

        Bukkit.broadcastMessage("data loaded, marking as rdy plugin " + mmoPlugin.getName());

        final var found = this.waiting.remove(mmoPlugin.getName());
        Validate.isTrue(found, String.format("Plugin %s already synced", mmoPlugin.getName()));

        // Wait for all plugins to load their data
        if (!this.waiting.isEmpty()) return;

        ////////////////////////////////
        // Player can start playing
        ////////////////////////////////
        this.state = PlayerSessionState.READY;
        this.playerData.startPlaying();
        Bukkit.broadcastMessage("========= START PLAYING " + this.toString());
    }

    @Override
    public String toString() {
        return "PlayerSession{" + "user=" + this.playerData.getUniqueId() + ", profileId=" + profileId + ", state=" + state + ", waiting=" + waiting + '}';
    }
}
