package io.lumine.mythic.lib.script.mechanic.misc;

import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.jetbrains.annotations.NotNull;

/**
 * Applies a cooldown to the skill caster
 */
@MechanicMetadata
public class ApplyCooldownMechanic extends Mechanic {
    private final String cooldownPath;
    private final NumericExpression amount;

    public ApplyCooldownMechanic(ConfigObject config) {
        cooldownPath = config.string("path", "p", "id", "name");
        amount = config.numericExpr("time", "t", "value", "val", "v", "amount", "amt", "a", "cooldown", "cd");
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        final double amount = this.amount.evaluate(meta);
        if (amount > 0) meta.getCaster().getData().getCooldownMap().applyCooldown(cooldownPath, amount);
    }
}
