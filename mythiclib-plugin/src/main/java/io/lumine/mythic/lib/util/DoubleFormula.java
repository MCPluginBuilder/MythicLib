package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Instead of directly using a double in a skill, we rather use a string
 * where internal or PAPI placeholders are parsed before finally
 * evaluating the formula. This represents 90% of the total skill
 * system configurability.
 *
 * @see NumericExpression
 */
@Deprecated
public class DoubleFormula {
    @Nullable
    private final String value;
    @Nullable
    private final Double constant;

    @Deprecated
    public static final DoubleFormula ZERO = DoubleFormula.constant(0);

    @Deprecated
    public DoubleFormula(@NotNull String inputFormula) {
        String value = null;
        Double constant = null;

        try {
            constant = Double.valueOf(inputFormula);
        } catch (IllegalArgumentException exception) {
            value = inputFormula;
        }

        this.value = value;
        this.constant = constant;
    }

    @Deprecated
    public DoubleFormula(double trivialValue) {
        this.value = null;
        this.constant = trivialValue;
    }

    @Deprecated
    public double evaluate(@NotNull SkillMetadata meta) {

        // Easy case
        if (constant != null) return constant;

        try {
            return NumericExpression.eval(meta.parseString(value));
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not evaluate '" + value + "': " + exception.getMessage());
            return 0;
        }
    }

    @Deprecated
    public static DoubleFormula constant(double value) {
        return new DoubleFormula(value);
    }
}
