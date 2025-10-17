package io.lumine.mythic.lib.data;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface OfflineDataHolder {

    /**
     * There is a strong difference with the {@link MMOPlayerData#getProfileId()} method.
     * This method should be using to store data that is relative to the
     * Bukkit player and not the current player's profile.
     * <p>
     * Typically, SQL methods shall use {@link MMOPlayerData#getProfileId()} as it provides
     * a simple way to differentiate between the different profiles of the same player.
     *
     * @return The current player's profile ID. If no profile plugin
     *         is installed, it just matches the player's UUID.
     * @return The original player's UUID.
     */
    @NotNull
    public UUID getUniqueId();
}
