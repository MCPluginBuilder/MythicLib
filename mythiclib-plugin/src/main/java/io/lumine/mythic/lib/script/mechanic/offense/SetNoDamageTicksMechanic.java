package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

@MechanicMetadata
public class SetNoDamageTicksMechanic extends TargetMechanic {
    private final NumericExpression ticks;
    private final boolean stack, min, max;

    public SetNoDamageTicksMechanic(ConfigObject config) {
        super(config);

        stack = config.bool(false, "stack", "add");
        min = config.getBoolean("min", false);
        max = config.getBoolean("max", false);
        ticks = config.numericExpr(NumericExpression.of(10), "ticks", "t", "duration", "dur", "d", "time");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof LivingEntity, "SetNoDamageTicksMechanic can only be applied to living entities");

        var ticks = (int) this.ticks.evaluate(meta);
        if (stack) ticks += ((LivingEntity) target).getNoDamageTicks();
        else if (max) ticks = Math.max(ticks, ((LivingEntity) target).getNoDamageTicks());
        else if (min) ticks = Math.min(ticks, ((LivingEntity) target).getNoDamageTicks());

        ((LivingEntity) target).setNoDamageTicks(ticks);
    }
}
