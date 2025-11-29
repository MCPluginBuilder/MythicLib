package io.lumine.mythic.lib.util.formula;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Lazy;
import org.jetbrains.annotations.NotNull;

/**
 * When MythicLib fails to precompile a numerical expression into a
 * NumericalExpression object, this class is used as a fallback.
 */
public class NaiveNumericalExpression extends NumericalExpression {
    private final String expression;

    public NaiveNumericalExpression(@NotNull String expression) {
        this.expression = expression;
    }

    public double evaluate(@NotNull Lazy<SkillMetadata> meta) {
        return NumericalExpression.eval(meta.get().parseString(this.expression));
    }
}
