package io.lumine.mythic.lib.script.mechanic.movement;

import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class VelocityMechanic extends TargetMechanic {
    private final String varName;

    public VelocityMechanic(ConfigObject config) {
        super(config);

        this.varName = config.string("value", "val", "v", "vector", "vec", "velocity", "vel");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        var velVar = meta.getVariable(varName);
        Validate.isTrue(velVar instanceof PositionVariable, "Variable '" + varName + "' is not a vector");
        Vector vel = ((PositionVariable) velVar).getStored().toVector();

        target.setVelocity(vel);
    }
}
