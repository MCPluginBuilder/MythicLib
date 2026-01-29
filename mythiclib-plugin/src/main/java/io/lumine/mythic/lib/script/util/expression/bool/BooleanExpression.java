package io.lumine.mythic.lib.script.util.expression.bool;

import io.lumine.mythic.lib.script.util.expression.AbstractExpression;
import org.jetbrains.annotations.NotNull;
import redempt.crunch.Crunch;

public abstract class BooleanExpression extends AbstractExpression {

    public static boolean eval(@NotNull String expression) {
        // maybe check for nullity directly instead of checking abs > EPSILON
        return Math.abs(Crunch.compileExpression(expression, ENV).evaluate()) > EPSILON;
    }
}
