package io.lumine.mythic.lib.profile;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.MMOPlugin;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
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
     * UUID of profile chosen
     */
    @Nullable
    private UUID profileId;

    /**
     * Turned off when player has either logged off or unselected
     * the profile provided.
     */
    private boolean alive = true;

    /**
     * Set to true when all external plugin data have been
     * loaded and the player has started playing
     */
    private boolean ready;

    /**
     * When logging in, MythicLib waits for all MMO plugins
     * to load their data before toggling on readiness flag
     * of this session object
     */
    @Nullable
    private List<String> waiting = new LinkedList<>();

    public ProfileSession(@NotNull MMOPlayerData playerData) {
        this.playerData = playerData;

        // Plugins we need to wait
        for (var mmoPlugin : MythicLib.plugin.getMMOPlugins())
            if (mmoPlugin.hasData()) waiting.add(mmoPlugin.getName());
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

    public boolean isAlive() {
        return alive;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isReady(@NotNull MMOPlugin mmoPlugin) {
        return this.ready || !this.waiting.contains(Objects.requireNonNull(mmoPlugin, "Plugin cannot be null").getName());
    }

    public void invalidate() {
        Validate.isTrue(alive, "Profile session already invalidated");
        this.alive = false;
    }

    public void markAsReady(@NotNull MMOPlugin mmoPlugin) {
        Validate.isTrue(mmoPlugin.hasData(), "This plugin has no data");
        Validate.isTrue(playerData.isOnline() || playerData.isLookup(), "Player went offline");
        Validate.isTrue(!isReady(), "Profile session already ready");
        Validate.isTrue(isAlive(), "Profile session is dead");

        final var found = this.waiting.remove(mmoPlugin.getName());
        Validate.isTrue(found, String.format("Plugin %s already synced", mmoPlugin.getName()));

        // Wait for all plugins to load their data
        if (!this.waiting.isEmpty()) return;

        ////////////////////////////////
        // Player can start playing
        ////////////////////////////////
        this.waiting = null; // Collect garbage
        this.ready = true;
        this.playerData.startPlaying();
    }
}
