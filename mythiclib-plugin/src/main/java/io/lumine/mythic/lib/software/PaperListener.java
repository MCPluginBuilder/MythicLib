package io.lumine.mythic.lib.software;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PaperListener implements Listener {

    @EventHandler
    public void a(PrePlayerAttackEntityEvent event) {
        var player = event.getPlayer();
        var playerData = MMOPlayerData.get(player);
        playerData.lastAttackCooldown = player.getAttackCooldown();
    }
}
