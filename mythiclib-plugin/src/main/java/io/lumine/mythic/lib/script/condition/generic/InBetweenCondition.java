package io.lumine.mythic.lib.script.condition.generic;

import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Checks if a given double is within some range.
 * The first value is included, the second is excluded.
 */
@Deprecated
public class InBetweenCondition extends Condition {
    private final NumericExpression first, second, third;

    @Deprecated
    public InBetweenCondition(ConfigObject config) {
        super(config);

        first = config.numericExpr("first");
        second = config.numericExpr("second");
        third = config.numericExpr("third");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        double middle = second.evaluate(meta);
        return first.evaluate(meta) <= middle && middle < third.evaluate(meta);
    }
}
