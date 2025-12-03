package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.api.stat.SharedStat;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class MovementSpeedStatHandler extends AttributeStatHandler {
    public MovementSpeedStatHandler(@NotNull ConfigurationSection config) {
        super(config, SharedStat.MOVEMENT_SPEED, .1, Material.LEATHER_BOOTS, "Movement speed of an Entity.");

        // Take into account Speed Malus Reduction
        setModifierEditor((instance, mod) -> {
            if (mod.getValue() >= 0) return mod;
            final var scalar = 1 - instance.getMap().getStat(SharedStat.SPEED_MALUS_REDUCTION) / 100;
            return mod.multiply(scalar);
        });
    }
}
