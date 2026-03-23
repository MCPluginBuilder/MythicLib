package io.lumine.mythic.lib.script.util.expression.numeric;

import io.lumine.mythic.lib.script.util.expression.placeholder.ExpressionPlaceholder;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Lazy;
import org.jetbrains.annotations.NotNull;
import redempt.crunch.Crunch;
import redempt.crunch.functional.EvaluationEnvironment;
import redempt.crunch.functional.Function;

public abstract class NumericExpression {

    public abstract double evaluate(@NotNull SkillMetadata skillMetadata);

    public abstract double evaluate(@NotNull Lazy<SkillMetadata> skillMetadata);

    //region Static methods

    public static NumericExpression ZERO = NumericExpression.of(0);
    public static NumericExpression ONE = NumericExpression.of(1);

    protected static final EvaluationEnvironment ENV = new EvaluationEnvironment();

    private static final Function RANDOM_DOUBLE = new Function("random", 0, args -> Math.random());
    private static final Function ATAN2 = new Function("atan2", 2, args -> Math.atan2(args[0], args[1]));
    private static final Function POW = new Function("pow", 2, args -> Math.pow(args[0], args[1]));
    private static final Function MIN = new Function("min", 2, args -> Math.min(args[0], args[1]));
    private static final Function MAX = new Function("max", 2, args -> Math.max(args[0], args[1]));
    private static final Function CLAMP = new Function("clamp", 3, args -> Math.min(args[2], Math.max(args[0], args[1])));
    private static final Function NON_ZERO = new Function("non_zero", 2, args -> args[0] == 0 ? args[1] : args[0]);

    protected static final double BOOLEAN_EPSILON = 1e-10;

    static {
        ENV.addFunction(RANDOM_DOUBLE);
        ENV.addFunction(ATAN2);
        ENV.addFunction(POW); // [Backwards compatibility]
        ENV.addFunction(MIN);
        ENV.addFunction(MAX);
        ENV.addFunction(CLAMP);
        ENV.addFunction(NON_ZERO);
        ENV.addLazyVariable("PI", () -> Math.PI); // [Backwards compatibility]
    }

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
        return Crunch.compileExpression(expression, ENV).evaluate();
    }

    public static boolean evalBoolean(@NotNull String expression) {
        return Crunch.compileExpression(expression, ENV).evaluate() > BOOLEAN_EPSILON;
    }

    @NotNull
    public static NumericExpression compile(@NotNull String expression,
                                            @NotNull java.util.function.Function<String, ExpressionPlaceholder> customPlaceholders) {
        return new PrecompiledNumericExpression(expression, customPlaceholders);
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
            return new PrecompiledNumericExpression(expression, null);
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

    //endregion
}
