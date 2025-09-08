package io.lumine.mythic.lib.profile.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.module.MMOPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.stream.Collectors;

public class NoProfileListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var playerData = MMOPlayerData.get(event.getPlayer());

        playerData.getProfileSession().startOpening(null, collectNamespacedKeys(), null);
    }

    private List<NamespacedKey> collectNamespacedKeys() {
        return MythicLib.plugin.getMMOPlugins().stream().map(MMOPlugin::getNamespacedKey).collect(Collectors.toList());
    }
}
