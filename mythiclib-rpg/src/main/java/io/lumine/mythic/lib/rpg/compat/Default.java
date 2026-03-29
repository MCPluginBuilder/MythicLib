package io.lumine.mythic.lib.rpg.compat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.ClassModule;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.jetbrains.annotations.NotNull;

public class Default implements LevelModule, ClassModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        return player.getPlayer().getLevel();
    }

    /**
     * Although there is no player class in vanilla Minecraft,
     * this method is implemented in the default MMOCore class
     * module so that the user can use 'default' in the config
     */
    @Override
    public @NotNull String getClass(@NotNull MMOPlayerData player) {
        return "None";
    }
}