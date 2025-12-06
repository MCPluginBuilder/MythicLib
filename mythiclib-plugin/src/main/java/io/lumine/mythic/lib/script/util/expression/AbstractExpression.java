package io.lumine.mythic.lib.script.util.expression;

import redempt.crunch.functional.EvaluationEnvironment;
import redempt.crunch.functional.Function;

public abstract class AbstractExpression {
    protected static final EvaluationEnvironment ENV = new EvaluationEnvironment();

    private static final Function RANDOM_DOUBLE = new Function("random", 0, args -> Math.random());
    private static final Function ATAN2 = new Function("atan2", 2, args -> Math.atan2(args[0], args[1]));
    private static final Function POW = new Function("pow", 2, args -> Math.pow(args[0], args[1]));
    private static final Function MIN = new Function("min", 2, args -> Math.min(args[0], args[1]));
    private static final Function MAX = new Function("max", 2, args -> Math.max(args[0], args[1]));
    private static final Function CLAMP = new Function("clamp", 3, args -> Math.min(args[2], Math.max(args[0], args[1])));
    private static final Function NON_ZERO = new Function("non_zero", 2, args -> args[0] == 0 ? args[1] : args[0]);

    protected static final double EPSILON = 1e-10;

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
}
