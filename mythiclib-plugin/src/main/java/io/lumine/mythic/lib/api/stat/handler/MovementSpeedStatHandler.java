package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.SharedStat;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.VersionUtils;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class MovementSpeedStatHandler extends AttributeStatHandler {
    public MovementSpeedStatHandler(@NotNull ConfigurationSection config) {
        super(config, SharedStat.MOVEMENT_SPEED, .1, Material.LEATHER_BOOTS, "Movement speed of an Entity.");

        addUpdateListener(this::updateMovementSpeedAttributeValue);
    }

    private void updateMovementSpeedAttributeValue(@NotNull StatInstance instance) {
        final AttributeInstance attrIns = instance.getMap().getData().getPlayer().getAttribute(Attributes.MOVEMENT_SPEED);
        removeModifiers(attrIns);

        // Calculate speed malus reduction (capped at 80%)
        final double coef = 1 - instance.getMap().getStat(SharedStat.SPEED_MALUS_REDUCTION) / 100;

        final double mmoFinal = clampValue(instance.getFilteredTotal(this.playerDefaultBase + this.baseValue, EquipmentSlot.MAIN_HAND::isCompatible, mod -> mod.getValue() < 0 ? mod.multiply(coef) : mod));
        final double difference = mmoFinal - this.playerDefaultBase;

        /*
         * Only add an attribute modifier if the very final stat
         * value is different from the main one to save calculations.
         */
        if (Math.abs(difference) > EPSILON)
            attrIns.addModifier(VersionUtils.attrMod(ATTRIBUTE_KEY, difference, AttributeModifier.Operation.ADD_NUMBER));
    }
}
