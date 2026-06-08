package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    /**
     * Async pre join events are unreliable for some reason so
     * it seems to be better to initialize player data on the
     * lowest priority possible synchronously when player join
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void setupPlayerDataOnLogin(PlayerJoinEvent event) {

        // Setup player data
        final var playerData = MMOPlayerData.setup(event.getPlayer());

        // [BACKWARDS COMPATIBILITY] Flush old modifiers
        UtilityMethods.flushOldModifiers(playerData.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void closeSessionOnLogOut(PlayerQuitEvent event) {
        final var playerData = MMOPlayerData.getOrNull(event.getPlayer());
        if (playerData != null) playerData.onLogout();
    }
}
