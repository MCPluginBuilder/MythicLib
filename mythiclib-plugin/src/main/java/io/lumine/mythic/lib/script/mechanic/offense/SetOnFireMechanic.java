package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.entity.Entity;

@MechanicMetadata
public class SetOnFireMechanic extends TargetMechanic {
    private final NumericExpression ticks;
    private final boolean stack, min, max;

    public SetOnFireMechanic(ConfigObject config) {
        super(config);

        stack = config.bool(false, "stack", "add");
        min = config.getBoolean("min", false);
        max = config.getBoolean("max", false);
        ticks = config.numericExpr("ticks", "t", "duration", "dur", "d", "time");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        var ticks = (int) this.ticks.evaluate(meta);
        if (stack) ticks += target.getFireTicks();
        else if (max) ticks = Math.max(ticks, target.getFireTicks());
        else if (min) ticks = Math.min(ticks, target.getFireTicks());

        target.setFireTicks(ticks);
    }
}
