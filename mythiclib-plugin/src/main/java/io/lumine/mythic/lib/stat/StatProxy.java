package io.lumine.mythic.lib.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StatProxy {
    private final String targetStat;
    private final ModifierType modifierType;
    private final double coefficient;
    private final NamespacedKey source;

    public StatProxy(NamespacedKey source, String targetStat, ModifierType modifierType, double coefficient) {
        this.source = source;
        this.targetStat = targetStat;
        this.modifierType = modifierType;
        this.coefficient = coefficient;
    }

    public StatProxy(ConfigurationSection config) {
        this.source = MythicLib.plugin.getNamespacedKey();
        this.targetStat = UtilityMethods.enumName(config.getName());
        var modifierTypeRw = Objects.requireNonNull(config.getString("type"), "Could not find 'type'");
        this.modifierType = UtilityMethods.prettyValueOf(ModifierType::valueOf, modifierTypeRw, "No modifier type '%s'");
        this.coefficient = config.getDouble("multiplier", 1);
    }

    @NotNull
    public NamespacedKey getSource() {
        return source;
    }

    @NotNull
    public String getTargetStat() {
        return targetStat;
    }

    public double getCoefficient() {
        return coefficient;
    }

    @NotNull
    public ProxyStatModifier newModifier(StatInstance parentInstance, EquipmentSlot actionHand) {
        return new ProxyStatModifier(parentInstance, this.targetStat, actionHand, this.modifierType, this.coefficient);
    }
}
