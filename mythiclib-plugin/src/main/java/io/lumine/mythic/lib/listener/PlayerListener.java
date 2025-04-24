package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

public class PlayerListener implements Listener {

    /**
     * Async pre join events are unreliable for some reason so
     * it seems to be better to initialize player data on the
     * lowest priority possible synchronously when player join
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void loadData(PlayerJoinEvent event) {

        // Setup player data
        final MMOPlayerData data = MMOPlayerData.setup(event.getPlayer());

        // [BACKWARDS COMPATIBILITY] Flush old modifiers
        UtilityMethods.flushOldModifiers(data.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuitLowest(PlayerQuitEvent event) {
        final MMOPlayerData playerData = MMOPlayerData.getOrNull(event.getPlayer());
        if (playerData != null) playerData.getStatMap().bufferUpdates();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitMonitor(PlayerQuitEvent event) {
        final MMOPlayerData playerData = MMOPlayerData.getOrNull(event.getPlayer());
        if (playerData != null) playerData.updatePlayer(null);
        else MythicLib.plugin.getLogger().log(Level.SEVERE, "Player data of " +
                event.getPlayer().getName() + " not loaded on logout. Were they kicked when joining the server?");
    }
}
