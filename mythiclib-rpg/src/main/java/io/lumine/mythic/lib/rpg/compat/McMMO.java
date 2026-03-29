package io.lumine.mythic.lib.rpg.compat;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.api.exceptions.McMMOPlayerNotFoundException;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.jetbrains.annotations.NotNull;

public class McMMO implements LevelModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        try {
            return ExperienceAPI.getPowerLevel(player.getPlayer());
        } catch (McMMOPlayerNotFoundException exception) {
            return 0;
        }
    }
}