package io.lumine.mythic.lib.skill.parameter.value;

import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

// TODO merge with NumericalExpression
public class LinearScalingFormula implements ScalingFormula {
    private final double base, perLevel, min, max;
    private final boolean hasMin, hasMax, integer;

    /**
     * A number formula which depends on the player level. It can be used
     * to handle skill modifiers so that the ability gets better with the
     * skill level, or as an attribute value to make them scale with the class level.
     *
     * @param base     Base value
     * @param perLevel Every level, final value is increased by X
     */
    public LinearScalingFormula(double base, double perLevel) {
        this.base = base;
        this.perLevel = perLevel;
        hasMin = false;
        hasMax = false;
        min = 0;
        max = 0;
        this.integer = false;
    }

    /**
     * A number formula which depends on the player level. It can be used
     * to handle skill modifiers so that the ability gets better with the
     * skill level, or as an attribute value to make them scale with the class level.
     *
     * @param base     Base value
     * @param perLevel Every level, final value is increased by X
     * @param min      Minimum final value
     * @param max      Maximum final value
     */
    public LinearScalingFormula(double base, double perLevel, double min, double max) {
        this.base = base;
        this.perLevel = perLevel;
        hasMin = true;
        hasMax = true;
        this.min = min;
        this.max = max;
        this.integer = false;
    }

    /**
     * Loads a linear formula from a config section
     *
     * @param config Config to load the formula from
     */
    public LinearScalingFormula(ConfigurationSection config) {

        // This fixes an issue with old, fucked up, skill configs
        // bc they were not checked before being put in production
        // Instead of sending a warning, just disable alltogether
        @BackwardsCompatibility(version = "1.31.1-SNAPSHOT")
        boolean safeguard = config.contains("min") && config.contains("max") && config.getDouble("min") >= config.getDouble("max");

        base = config.getDouble("base");
        perLevel = config.getDouble("per-level");
        hasMin = !safeguard && config.contains("min");
        hasMax = !safeguard && config.contains("max");
        min = hasMin ? config.getDouble("min") : 0;
        max = hasMax ? config.getDouble("max") : 0;
        integer = config.getBoolean("int");

        //if (hasMin && hasMax && min >= max) throw new IllegalArgumentException("Min is higher or equal to max value");
    }

    @Override
    public double evaluate(int skillLevel, @Nullable Player player) {
        double value = base + perLevel * (skillLevel - 1);
        if (hasMin) value = Math.max(min, value);
        if (hasMax) value = Math.min(max, value);
        return value;
    }

    @Override
    public boolean isInteger() {
        return integer;
    }
}
