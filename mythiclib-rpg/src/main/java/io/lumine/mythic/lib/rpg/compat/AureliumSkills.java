package io.lumine.mythic.lib.rpg.compat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AureliumSkills implements LevelModule {
    private final com.archyx.aureliumskills.AureliumSkills plugin;

    public AureliumSkills() {
        plugin = (com.archyx.aureliumskills.AureliumSkills) Bukkit.getPluginManager().getPlugin("AureliumSkills");
        Objects.requireNonNull(plugin, "ASkills not found");
    }

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        var other = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (other == null) return 1;
        return other.getPowerLevel();
    }
}