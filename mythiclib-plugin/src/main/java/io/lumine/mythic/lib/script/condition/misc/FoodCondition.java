package io.lumine.mythic.lib.script.condition.misc;

import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Checks if the current world time is DAY/NIGHT/DUSK..
 */
public class FoodCondition extends Condition {
    private final NumericExpression amount;

    public FoodCondition(ConfigObject config) {
        super(config);

        amount = config.numericExpr("amount");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        return meta.getCaster().getPlayer().getFoodLevel() >= amount.evaluate(meta);
    }
}