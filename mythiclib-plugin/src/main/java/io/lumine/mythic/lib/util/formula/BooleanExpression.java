package io.lumine.mythic.lib.util.formula;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;
import org.jetbrains.annotations.NotNull;

public abstract class BooleanExpression {

    private static final double EPSILON = 1e-10;

    // Logical operators
    private static final int EQUALITY_PRECEDENCE = 100;
    private static final int ORDER_PRECEDENCE = 300;
    private static final Operator LOGICAL_AND = new Operator("&&", 2, true, Operator.PRECEDENCE_MULTIPLICATION) {

        @Override
        public double apply(double... args) {
            return ((args[0] != 0) && (args[1] != 0)) ? 1 : 0;
        }
    };
    private static final Operator LOGICAL_OR = new Operator("||", 2, true, Operator.PRECEDENCE_ADDITION) {

        @Override
        public double apply(double... args) {
            return ((args[0] != 0) || (args[1] != 0)) ? 1 : 0;
        }
    };
    private static final Operator LOGICAL_NOT = new Operator("!", 1, true, Operator.PRECEDENCE_ADDITION) {

        @Override
        public double apply(double... args) {
            return args[0] != 0 ? 0 : 1;
        }
    };

    // Equality operators
    private static final Operator LOGICAL_EQ = new Operator("==", 2, true, EQUALITY_PRECEDENCE) {

        @Override
        public double apply(double... args) {
            return Math.abs(args[0] - args[1]) < EPSILON ? 1 : 0;
        }
    };
    private static final Operator LOGICAL_NEQ = new Operator("!=", 2, true, EQUALITY_PRECEDENCE) {

        @Override
        public double apply(double... args) {
            return Math.abs(args[0] - args[1]) > EPSILON ? 1 : 0;
        }
    };

    // Comparators
    private static final Operator LOWER_OR_EQUAL = new Operator("<=", 2, false, ORDER_PRECEDENCE) {
        @Override
        public double apply(double... doubles) {
            final double d1 = doubles[0], d2 = doubles[1];
            return d1 <= d2 || Math.abs(d1 - d2) < EPSILON ? 1 : 0;
        }
    };
    private static final Operator LOWER_THAN = new Operator("<", 2, false, ORDER_PRECEDENCE) {
        @Override
        public double apply(double... doubles) {
            return doubles[0] < doubles[1] ? 1 : 0;
        }
    };
    private static final Operator GREATER_OR_EQUAL = new Operator(">=", 2, false, ORDER_PRECEDENCE) {
        @Override
        public double apply(double... doubles) {
            final double d1 = doubles[0], d2 = doubles[1];
            return d1 >= d2 || Math.abs(d1 - d2) < EPSILON ? 1 : 0;
        }
    };
    private static final Operator GREATER_THAN = new Operator(">", 2, false, ORDER_PRECEDENCE) {
        @Override
        public double apply(double... doubles) {
            return doubles[0] > doubles[1] ? 1 : 0;
        }
    };
    private static final Operator[] OPERATORS = {
            LOGICAL_NOT, LOGICAL_OR, LOGICAL_AND,
            LOGICAL_EQ, LOGICAL_NEQ,
            LOWER_OR_EQUAL, LOWER_THAN, GREATER_OR_EQUAL, GREATER_THAN};

    // Logical constants
    private static final String[] CONSTANTS = {"true", "false"};

    @NotNull
    protected static Expression decorateAndCompile(@NotNull ExpressionBuilder builder) {
        return builder
                .implicitMultiplication(false)
                .operator(OPERATORS)
                .variables(CONSTANTS)
                .build() // Heavy expression compilation call
                .setVariable("true", 1)
                .setVariable("false", 0);
    }

    public static boolean eval(@NotNull String expression) {
        // TODO precompile boolean formulas
        // TODO add all functions to boolean formulas as well
        return decorateAndCompile(new ExpressionBuilder(expression)).evaluate() != 0;
    }
}
