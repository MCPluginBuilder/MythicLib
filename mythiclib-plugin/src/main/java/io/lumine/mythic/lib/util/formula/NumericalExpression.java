package io.lumine.mythic.lib.util.formula;

import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class NumericalExpression {

    /**
     * @param expression Expression to evaluate
     * @see NumericExpression#evaluate(SkillMetadata)
     * @deprecated
     */
    @Deprecated
    public static double eval(@NotNull String expression) {
        return NumericExpression.eval(expression);
    }
}
