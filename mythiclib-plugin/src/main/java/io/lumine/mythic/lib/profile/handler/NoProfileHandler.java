package io.lumine.mythic.lib.profile.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoProfileHandler implements ProfileHandler {
    private final List<NamespacedKey> modules;

    public NoProfileHandler() {
        modules = MythicLib.plugin.getMMOPlugins().stream()
                .filter(MMOPlugin::hasData)
                .map(MMOPlugin::getNamespacedKey)
                .collect(Collectors.toList());
    }

    @Override
    public void onStartup() {
        // TODO improve implementation
        Bukkit.getScheduler().runTaskLater(MythicLib.plugin, () -> {
            MMOPlayerData.getLoaded().forEach(data -> data.chooseProfile(null, SessionUpdateReason.LOGIN));
        }, 20);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLogin(PlayerJoinEvent event) {
        // Runs on priority LOW as MMO plugin player datas are initialized on priority LOWEST
        MMOPlayerData.get(event.getPlayer()).chooseProfile(null, SessionUpdateReason.LOGIN);
    }

    @Override
    public List<NamespacedKey> collectModules() {
        return new ArrayList<>(this.modules);
    }
}
