package io.lumine.mythic.lib.rpg.compat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.ClassModule;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class Skills implements LevelModule, ClassModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        var other = me.leothepro555.skills.main.Skills.get().getPlayerDataManager().getOrLoadPlayerInfo(player.getPlayer());
        return other.getLevel();
    }

    @Override
    public @NotNull String getClass(@NotNull MMOPlayerData player) {
        var other = me.leothepro555.skills.main.Skills.get().getPlayerDataManager().getOrLoadPlayerInfo(player.getPlayer());
        return ChatColor.stripColor(other.getSkill().getLanguageName().getDefault());
    }
}