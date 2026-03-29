package io.lumine.mythic.lib.rpg.compat;

import com.sucy.skill.SkillAPI;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.ClassModule;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.jetbrains.annotations.NotNull;

public class ProSkillAPI implements LevelModule, ClassModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        var other = SkillAPI.getPlayerAccountData(player.getPlayer()).getActiveData();
        var mainClass = other.getMainClass();
        return mainClass == null ? 0 : mainClass.getLevel();
    }

    @Override
    public @NotNull String getClass(@NotNull MMOPlayerData player) {
        var other = SkillAPI.getPlayerAccountData(player.getPlayer()).getActiveData();
        var mainClass = other.getMainClass();
        return mainClass == null ? "" : mainClass.getData().getName();
    }
}