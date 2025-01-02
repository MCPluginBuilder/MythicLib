package io.lumine.mythic.lib.listener.option;

import io.lumine.mythic.lib.version.Attributes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FixMovementSpeed implements Listener {

    @EventHandler
    public void a(PlayerJoinEvent event) {
        event.getPlayer().getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.1);
    }
}
