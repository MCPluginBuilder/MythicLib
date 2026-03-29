package io.lumine.mythic.lib.rpg.compat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.ClassModule;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.jetbrains.annotations.NotNull;

public class Fabled implements LevelModule, ClassModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        var other = studio.magemonkey.fabled.Fabled.getData(player.getPlayer());
        var mainClass = other.getMainClass();
        return mainClass == null ? 0 : mainClass.getLevel();
    }

    @Override
    public @NotNull String getClass(@NotNull MMOPlayerData player) {
        var other = studio.magemonkey.fabled.Fabled.getData(player.getPlayer());
        var mainClass = other.getMainClass();
        return mainClass == null ? "" : mainClass.getData().getName();
    }
}