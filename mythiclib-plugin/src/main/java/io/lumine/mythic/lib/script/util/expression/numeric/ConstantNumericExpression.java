package io.lumine.mythic.lib.script.util.expression.numeric;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Lazy;
import org.jetbrains.annotations.NotNull;

/**
 * When MythicLib fails to precompile a numerical expression into a
 * NumericalExpression object, this class is used as a fallback.
 */
public class ConstantNumericExpression extends NumericExpression {
    private final double constantValue;

    @Deprecated
    public static NumericExpression ZERO = NumericExpression.ZERO;
    @Deprecated
    public static NumericExpression ONE = NumericExpression.ONE;

    public ConstantNumericExpression(double constantValue) {
        this.constantValue = constantValue;
    }

    @Override
    public double evaluate(@NotNull SkillMetadata skillMetadata) {
        return this.constantValue;
    }

    public double evaluate(@NotNull Lazy<SkillMetadata> meta) {
        return this.constantValue;
    }
}
