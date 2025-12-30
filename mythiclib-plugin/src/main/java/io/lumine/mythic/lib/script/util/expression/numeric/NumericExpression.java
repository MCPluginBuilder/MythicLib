package io.lumine.mythic.lib.script.util.expression.numeric;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.util.expression.AbstractExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Lazy;
import org.jetbrains.annotations.NotNull;
import redempt.crunch.Crunch;

public abstract class NumericExpression extends AbstractExpression {

    public abstract double evaluate(@NotNull SkillMetadata skillMetadata);

    public abstract double evaluate(@NotNull Lazy<SkillMetadata> skillMetadata);

    /**
     * This is a very lazy implementation of numerical expression computation.
     * This method both compiles and evaluates the given numerical expression.
     * For better performance, do consider pre-processing and pre-compiling the
     * expression before evaluating it.
     * <p>
     * This method will throw RuntimeExceptions if compilation or evaluation fails.
     *
     * @param expression Numerical expression
     * @return Value of numerical expression
     */
    public static double eval(@NotNull String expression) {
        return Crunch.evaluateExpression(expression);
    }

    @NotNull
    public static NumericExpression compile(@NotNull String expression) {

        // Try as constant
        try {
            final var constantValue = Double.parseDouble(expression);
            return new ConstantNumericExpression(constantValue);
        } catch (NumberFormatException ignored) {
            // Not a constant
        }

        // Try to precompile
        try {
            return new PrecompiledNumericExpression(expression);
        } catch (Exception e) {
            // Fail silently
            //MythicLib.plugin.debug("PRECOMPILE FAILURE");
            //e.printStackTrace();
        }

        // Runtime compilation and evaluation fallback
        // Worst for performance but 100% sound
        return new NaiveNumericExpression(expression);
    }

    @NotNull
    public static NumericExpression of(double constantValue) {
        return new ConstantNumericExpression(constantValue);
    }
}
