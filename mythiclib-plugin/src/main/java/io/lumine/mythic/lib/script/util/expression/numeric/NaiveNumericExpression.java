package io.lumine.mythic.lib.script.util.expression.numeric;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Lazy;
import org.jetbrains.annotations.NotNull;

/**
 * When MythicLib fails to precompile a numerical expression into a
 * NumericalExpression object, this class is used as a fallback.
 * <p>
 * Precompiled numeric expressions do not support recursive PAPI
 * placeholders, for this reason users are advised to use existing
 * math primitives instead of recursive PAPI placeholders for performance.
 *
 * @author jules
 */
public class NaiveNumericExpression extends NumericExpression {
    private final String expression;

    public NaiveNumericExpression(@NotNull String expression) {
        this.expression = expression;
    }

    public double evaluate(@NotNull Lazy<SkillMetadata> meta) {
        return NumericExpression.eval(meta.get().parseString(this.expression));
    }

    @Override
    public double evaluate(@NotNull SkillMetadata skillMetadata) {
        return NumericExpression.eval(skillMetadata.parseString(this.expression));
    }
}
