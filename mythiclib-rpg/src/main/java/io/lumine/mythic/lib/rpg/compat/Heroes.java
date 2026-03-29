package io.lumine.mythic.lib.rpg.compat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.ClassModule;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.jetbrains.annotations.NotNull;

public class Heroes implements LevelModule, ClassModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        var other = com.herocraftonline.heroes.Heroes.getInstance().getCharacterManager().getHero(player.getPlayer());
        return other.getHeroLevel();
    }

    @Override
    public @NotNull String getClass(@NotNull MMOPlayerData player) {
        var other = com.herocraftonline.heroes.Heroes.getInstance().getCharacterManager().getHero(player.getPlayer());
        return other.getHeroClass().getName();
    }
}