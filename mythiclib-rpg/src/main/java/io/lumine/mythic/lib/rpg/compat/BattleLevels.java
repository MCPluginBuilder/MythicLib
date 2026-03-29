package io.lumine.mythic.lib.rpg.compat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.LevelModule;
import me.robin.battlelevels.api.BattleLevelsAPI;
import org.jetbrains.annotations.NotNull;

public class BattleLevels implements LevelModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        return BattleLevelsAPI.getLevel(player.getUniqueId());
    }
}