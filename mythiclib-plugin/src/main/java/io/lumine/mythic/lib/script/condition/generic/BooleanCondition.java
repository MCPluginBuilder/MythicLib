package io.lumine.mythic.lib.script.condition.generic;

import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.script.util.expression.bool.BooleanExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Checks if the specified algebraic expression returns true
 */
public class BooleanCondition extends Condition {
    private final String formula;

    public BooleanCondition(ConfigObject config) {
        super(config);

        formula = config.string("formula", "form", "f", "expression", "expr", "e");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        return BooleanExpression.eval(meta.parseString(formula));
    }
}