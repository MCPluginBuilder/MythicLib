package io.lumine.mythic.lib.data;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.profile.ProfileMode;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class is implemented by player datas. A small boolean field
 * is absolutely necessary to keep track if player data has already
 * been loaded or not.
 * <p>
 * This class used to contain a reference to a MMOPlayerData instance.
 * Since it only needs the UUID used to save player data, the only
 * data it really needs is a profile ID.
 *
 * @author jules
 */
public abstract class SynchronizedDataHolder implements OfflineDataHolder {
    private final MMOPlayerData playerData;
    private final MMOPlugin mmoPlugin;

    private boolean profileSessionReady;

    /**
     * @param mmoPlugin  If the plugin creating the player data is a profile plugin
     * @param playerData Parent MythicLib player data
     */
    public SynchronizedDataHolder(@NotNull MMOPlugin mmoPlugin, @NotNull MMOPlayerData playerData) {
        this.mmoPlugin = mmoPlugin;
        this.playerData = playerData;
    }

    @NotNull
    public MMOPlayerData getMMOPlayerData() {
        return playerData;
    }

    @Override
    @NotNull
    public UUID getUniqueId() {
        return playerData.getUniqueId();
    }

    @NotNull
    public UUID getProfileId() {
        return playerData.getProfileId();
    }

    @NotNull
    public UUID getOfficialId() {
        return playerData.getOfficialId();
    }

    @NotNull
    public Player getPlayer() {
        return playerData.getPlayer();
    }

    /**
     * @return The UUID used to save player data inside a database.
     */
    @NotNull
    public UUID getEffectiveId() {

        // No profiles => All IDs match
        if (MythicLib.plugin.getProfileMode() == ProfileMode.NONE) return getUniqueId();

        // Profile plugin
        if (mmoPlugin.isProfilePlugin()) {
            // Proxy mode => take official Mojang ID
            if (MythicLib.plugin.getProfileMode() == ProfileMode.PROXY) return getOfficialId();
            // Legacy profiles, all UUIDs match, take entity ID
            if (MythicLib.plugin.getProfileMode() == ProfileMode.LEGACY) return getUniqueId();
            throw new RuntimeException("Unhandled profile mode");
        }

        // Otherwise, take profile ID if it exists
        // TODO validate a profile has been chosen??? #getUniqueID() should not be used
        return playerData.hasProfile() ? getProfileId() : getUniqueId();
    }

    /**
     * Called before the player data is autosaved to the database.
     */
    public void onAutosave() {
        // Nothing by default
    }

    public void onClose(@NotNull SaveReason reason) {
        // Nothing by default
    }

    /**
     * @return True if this particular player data has been successfully loaded
     *         from the database
     * @see MMOPlayerData#hasStartedPlaying()
     */
    public boolean isSessionReady() {
        if (mmoPlugin.isProfilePlugin()) {
            return profileSessionReady;
        }

        return playerData.getProfileSession().isReady(mmoPlugin.getNamespacedKey());
    }

    public void markSessionReady() {
        Bukkit.broadcastMessage("marking session ready for plugin " + mmoPlugin.getName() + " for player " + playerData.getPlayer().getName() + " :: " + mmoPlugin.getNamespacedKey() + " / " + mmoPlugin.isProfilePlugin());
        if (mmoPlugin.isProfilePlugin()) {
            Validate.isTrue(!this.profileSessionReady, "Profile session already ready");
            this.profileSessionReady = true;
            return;
        }

        playerData.getProfileSession().markAsReady(mmoPlugin.getNamespacedKey());
    }

    public void markSessionClosed() {
        playerData.getProfileSession().markAsClosed(mmoPlugin.getNamespacedKey());
    }

    //region Deprecated

    @Deprecated
    public boolean isSynchronized() {
        return isSessionReady();
    }

    @Deprecated
    public void markAsSynchronized() {
        markSessionReady();
    }

    @Deprecated
    public boolean shouldBeSaved() {
        return isSessionReady();
    }

    //endregion
}
