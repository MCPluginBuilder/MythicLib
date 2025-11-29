package io.lumine.mythic.lib.util.formula;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Lazy;
import org.jetbrains.annotations.NotNull;

/**
 * When MythicLib fails to precompile a numerical expression into a
 * NumericalExpression object, this class is used as a fallback.
 */
public class ConstantNumericalExpression extends NumericalExpression {
    private final double constantValue;

    public ConstantNumericalExpression(double constantValue) {
        this.constantValue = constantValue;
    }

    public double evaluate(@NotNull Lazy<SkillMetadata> meta) {
        return this.constantValue;
    }
}
