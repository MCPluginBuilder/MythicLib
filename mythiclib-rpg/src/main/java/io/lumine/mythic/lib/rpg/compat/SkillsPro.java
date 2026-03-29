package io.lumine.mythic.lib.rpg.compat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.ClassModule;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class SkillsPro implements LevelModule, ClassModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        var other = org.skills.main.SkillsPro.get().getPlayerDataManager().getData(player.getUniqueId());
        return other.getLevel();
    }

    @Override
    public @NotNull String getClass(@NotNull MMOPlayerData player) {
        var other = org.skills.main.SkillsPro.get().getPlayerDataManager().getData(player.getUniqueId());
        return ChatColor.stripColor(other.getSkill().getDisplayName());
    }
}