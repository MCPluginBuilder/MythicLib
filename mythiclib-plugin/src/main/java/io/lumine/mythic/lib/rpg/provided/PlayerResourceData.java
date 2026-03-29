package io.lumine.mythic.lib.rpg.provided;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.SharedStat;
import io.lumine.mythic.lib.player.PlayerDataMap;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class PlayerResourceData extends PlayerDataMap {
    private final MMOPlayerData parent;
    private boolean initialized;

    private double mana, stamina;

    public PlayerResourceData(MMOPlayerData parent) {
        this.parent = parent;
    }

    @Override
    protected void onSessionOpen() {

        // Initialize resource values
        if (!initialized) {
            initialized = true;

            // Default resource ratios
            this.mana = MythicLib.plugin.getMMOConfig().manaLoginRatio * parent.getStatMap().getStat(SharedStat.MAX_MANA);
            this.stamina = MythicLib.plugin.getMMOConfig().staminaLoginRatio * parent.getStatMap().getStat(SharedStat.MAX_STAMINA);
        }
    }

    @NotNull
    public MMOPlayerData getParent() {
        return parent;
    }

    public double getMana() {
        return mana;
    }

    public double getStamina() {
        return stamina;
    }

    public boolean setMana(double amount, @NotNull ResourceUpdateReason reason) {

        final var maxValue = parent.getStatMap().getStat(SharedStat.MAX_MANA);
        var newValue = Math.max(0, Math.min(amount, maxValue));
        if (mana == newValue) return true;

        if (reason != ResourceUpdateReason.CHOOSE_CLASS) {
            final var called = new ResourceUpdateEvent(this.parent, this.mana, newValue, reason, PlayerResource.MANA);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled()) return false;
            newValue = called.getNewAmount();
        }

        mana = Math.max(0, Math.min(newValue, maxValue));
        return true;
    }

    public boolean setStamina(double amount, @NotNull ResourceUpdateReason reason) {

        final var maxValue = parent.getStatMap().getStat(SharedStat.MAX_STAMINA);
        var newValue = Math.max(0, Math.min(amount, maxValue));
        if (stamina == newValue) return true;

        if (reason != ResourceUpdateReason.CHOOSE_CLASS) {
            final var called = new ResourceUpdateEvent(this.parent, this.stamina, newValue, reason, PlayerResource.STAMINA);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled()) return false;
            newValue = called.getNewAmount();
        }

        stamina = Math.max(0, Math.min(newValue, maxValue));
        return false;
    }

    public void giveMana(double amount, @NotNull ResourceUpdateReason reason) {
        setMana(mana + amount, reason);
    }

    public void giveStamina(double amount, @NotNull ResourceUpdateReason reason) {
        setStamina(stamina + amount, reason);
    }
}
