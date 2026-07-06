package io.lumine.mythic.lib.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public class StatProxy {
    private final String targetStat;
    private final ModifierType modifierType;
    private final double coefficient;

    public StatProxy(String targetStat, ModifierType modifierType, double coefficient) {
        this.targetStat = targetStat;
        this.modifierType = modifierType;
        this.coefficient = coefficient;
    }

    public StatProxy(ConfigurationSection config) {
        this.targetStat = UtilityMethods.enumName(config.getName());
        var modifierTypeRw = Objects.requireNonNull(config.getString("type"), "Could not find 'type'");
        this.modifierType = UtilityMethods.prettyValueOf(ModifierType::valueOf, modifierTypeRw, "No modifier type '%s'");
        this.coefficient = config.getDouble("multiplier", 1);
    }

    public String getTargetStat() {
        return targetStat;
    }

    public ProxyStatModifier newModifier(StatInstance parentInstance, EquipmentSlot actionHand) {
        return new ProxyStatModifier(parentInstance, this.targetStat, actionHand, this.modifierType, this.coefficient);
    }
}
