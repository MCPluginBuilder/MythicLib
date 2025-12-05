package io.lumine.mythic.lib.skill.parameter.value;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO merge with NumericalExpression
public interface ScalingFormula {

    public double evaluate(int skillLevel, @Nullable Player player);

    public boolean isInteger();

    public static ScalingFormula fromConfig(@Nullable Object object) {
        return fromConfig(object, null);
    }

    /**
     * Defines a YAML syntax block for a skill parameter value
     *
     * @param object YML config object. Either a config, string or number. If this argument
     *               is null, it will return a skill parameter with a constant value of 0.
     */
    @NotNull
    public static ScalingFormula fromConfig(@Nullable Object object, @Nullable ScalingFormula reference) {

        // null -> ZERO
        if (object == null) return ZERO;

        if (object instanceof Number) return new NonScalingFormula(((Number) object).doubleValue());

        if (object instanceof ConfigurationSection) {
            var config = (ConfigurationSection) object;
            if (config.contains("base")) return new LinearScalingFormula(config);

            if (config.contains("formula")) return new CustomScalingFormula(config, reference);

            throw new IllegalArgumentException("Skill parameter formula must contain either 'base' or 'formula' as key");
        }

        if (object instanceof String) return new CustomScalingFormula((String) object);

        throw new IllegalArgumentException("Skill parameter formula must be a string, number of config section");
    }

    public static final ScalingFormula ZERO = new NonScalingFormula(0);
}
