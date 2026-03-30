package io.lumine.mythic.lib.rpg.provided;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import io.lumine.mythic.lib.rpg.ClassModule;
import io.lumine.mythic.lib.rpg.LevelModule;
import io.lumine.mythic.lib.rpg.ManaModule;
import org.jetbrains.annotations.NotNull;

public class DummyModule implements ClassModule, LevelModule, ManaModule {

    public static DummyModule INSTANCE = new DummyModule();

    @Override
    public @NotNull String getClass(@NotNull MMOPlayerData playerData) {
        return "";
    }

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        return player.getPlayer().getLevel();
    }

    @Override
    public boolean setMana(@NotNull MMOPlayerData player, double newValue, @NotNull ResourceUpdateReason reason) {
        player.getPlayer().setFoodLevel((int) newValue);
        return true;
    }

    @Override
    public boolean setStamina(@NotNull MMOPlayerData player, double newValue, @NotNull ResourceUpdateReason reason) {
        return false;
    }

    @Override
    public double getMana(@NotNull MMOPlayerData player) {
        return player.getPlayer().getFoodLevel();
    }

    @Override
    public double getStamina(@NotNull MMOPlayerData player) {
        return 0;
    }
}
