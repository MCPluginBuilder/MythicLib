package io.lumine.mythic.lib.script.condition.misc;

import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

public class RandomChanceCondition extends Condition {
    private final NumericExpression chance;

    public RandomChanceCondition(ConfigObject config) {
        super(config);

        this.chance = config.numericExpr("chance", "c", "percentage", "percent", "p");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        return Math.random() < chance.evaluate(meta);
    }
}
