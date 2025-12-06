package io.lumine.mythic.lib.skill.parameter.value;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// TODO merge with NumericalExpression
public class CustomScalingFormula implements ScalingFormula {

    @NotNull
    private final String expression;
    private final double failsafe;

    //@Nullable
    //private final String display;

    /**
     * Disable PAPI placeholders if needed
     */
    private final boolean papi;

    private final boolean integer;

    public CustomScalingFormula(@NotNull String expression) {
        this.expression = expression;
        //this.display = null;
        this.papi = expression.contains("%"); // Small optimization
        this.integer = false;
        this.failsafe = 0;
    }

    public CustomScalingFormula(@NotNull ConfigurationSection config, @Nullable ScalingFormula reference) {
        this.expression = Objects.requireNonNull(config.getString("formula"), "Formula cannot be null");
        //this.display = config.getString("display");
        this.papi = config.getBoolean("papi", true);
        this.integer = (reference != null && reference.isInteger()) || config.getBoolean("int");
        this.failsafe = config.getDouble("failsafe");
    }

    //@NotNull
    //public String getDisplay() {
    //    return Objects.requireNonNullElse(display, expression);
    //}

    @Override
    public double evaluate(int skillLevel, @Nullable Player player) {
        try {

            // Parse skill level
            var parsed = this.expression.replace("{level}", String.valueOf(skillLevel));

            // Parse PAPI only
            // [BACKWARDS COMPATIBILITY] playerData can be null sometimes.
            if (papi && player != null) parsed = MythicLib.plugin.getPlaceholderParser().parse(player, parsed);

            // Evaluate formula
            return NumericExpression.eval(parsed);

        } catch (RuntimeException exception) {
            throw new FormulaFailsafeException(exception, failsafe);
        }
    }

    @Override
    public boolean isInteger() {
        return integer;
    }
}
