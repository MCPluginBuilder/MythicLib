package io.lumine.mythic.lib.rpg.provided;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.SharedStat;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import io.lumine.mythic.lib.rpg.ManaModule;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class NativeManaModule implements ManaModule {
    private final double regenCoefficient;

    public NativeManaModule() {
        var period = Math.max(1, MythicLib.plugin.getMMOConfig().manaRefreshRate);
        this.regenCoefficient = (double) period / 20d;

        // Register loop for natural mana and stamina regeneration
        // Wait a few seconds after server startup
        Bukkit.getScheduler().runTaskTimer(MythicLib.plugin, this::tickOnlinePlayers, 5 * 20L, period);
    }

    private void tickOnlinePlayers() {
        MMOPlayerData.forEachPlaying(player -> {
            player.getResources().giveMana(player.getStatMap().getStat(SharedStat.MANA_REGENERATION) * regenCoefficient, ResourceUpdateReason.REGENERATION);
            player.getResources().giveStamina(player.getStatMap().getStat(SharedStat.STAMINA_REGENERATION) * regenCoefficient, ResourceUpdateReason.REGENERATION);
        });
    }

    @Override
    public boolean setMana(@NotNull MMOPlayerData playerData, double newValue, @NotNull ResourceUpdateReason reason) {
        return playerData.getResources().setMana(newValue, reason);
    }

    @Override
    public boolean setStamina(@NotNull MMOPlayerData playerData, double newValue, @NotNull ResourceUpdateReason reason) {
        return playerData.getResources().setStamina(newValue, reason);
    }

    @Override
    public double getMana(@NotNull MMOPlayerData playerData) {
        return playerData.getResources().getMana();
    }

    @Override
    public double getStamina(@NotNull MMOPlayerData playerData) {
        return playerData.getResources().getStamina();
    }
}
