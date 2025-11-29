package io.lumine.mythic.lib.util.formula;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Lazy;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class NumericalExpression {

    // Built-in functions
    private static final Function RANDOM_DOUBLE = new Function("random", 0) {
        @Override
        public double apply(double... doubles) {
            return Math.random();
        }
    };
    private static final Function ATAN2 = new Function("atan2", 2) {
        @Override
        public double apply(double... doubles) {
            return Math.atan2(doubles[0], doubles[1]);
        }
    };
    private static final Function POW = new Function("pow", 2) {
        @Override
        public double apply(double... doubles) {
            return Math.pow(doubles[0], doubles[1]);
        }
    };
    private static final Function MIN = new Function("min", 2) {
        @Override
        public double apply(double... doubles) {
            return Math.min(doubles[0], doubles[1]);
        }
    };
    private static final Function MAX = new Function("max", 2) {
        @Override
        public double apply(double... doubles) {
            return Math.max(doubles[0], doubles[1]);
        }
    };
    private static final Function CLAMP = new Function("clamp", 3) {
        @Override
        public double apply(double... doubles) {
            // clamp(x, min, max) = min(max, max(x, min))
            return Math.min(doubles[2], Math.max(doubles[0], doubles[1]));
        }
    };
    private static final Function NON_ZERO = new Function("non_zero", 2) {
        @Override
        public double apply(double... doubles) {
            return doubles[0] == 0 ? doubles[1] : doubles[0];
        }
    };
    private static final Function[] FUNCTIONS = {RANDOM_DOUBLE, ATAN2, POW, MIN, MAX, NON_ZERO, CLAMP};

    // Constants
    private static final Map<String, Double> CONSTANTS;

    static {
        final Map<String, Double> constants = new HashMap<>();
        constants.put("PI", Math.PI);
        constants.put("Pi", Math.PI);
        final double phi = .5 * (1 + Math.sqrt(5));
        constants.put("phi", phi);
        constants.put("Phi", phi);
        constants.put("PHI", phi);
        CONSTANTS = Collections.unmodifiableMap(constants);
    }

    public abstract double evaluate(@NotNull Lazy<SkillMetadata> skillMetadata);

    @NotNull
    protected static Expression decorateAndCompile(@NotNull ExpressionBuilder builder) {
        return builder
                .implicitMultiplication(false)
                .functions(FUNCTIONS)
                .variables(CONSTANTS.keySet())
                .build()
                .setVariables(CONSTANTS);
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
        return decorateAndCompile(new ExpressionBuilder(expression)).evaluate();
    }

    @NotNull
    public static NumericalExpression compile(@NotNull String expression) {

        // Try as constant
        try {
            final var constantValue = Double.parseDouble(expression);
            return new ConstantNumericalExpression(constantValue);
        } catch (NumberFormatException ignored) {
        }

        // Try to precompile
        try {
            return new PrecompiledNumericalExpression(expression);
        } catch (Exception e) {
            /*
            MythicLib.plugin.getLogger().log(Level.WARNING, "Precompile failure");
            e.printStackTrace();
             */
        }

        // Runtime compilation and evaluation fallback
        // Worst for performance but 100% sound
        return new NaiveNumericalExpression(expression);
    }
}
