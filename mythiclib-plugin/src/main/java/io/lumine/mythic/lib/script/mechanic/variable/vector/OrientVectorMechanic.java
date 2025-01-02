package io.lumine.mythic.lib.script.mechanic.variable.vector;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Position;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.util.Vector;

@MechanicMetadata
public class OrientVectorMechanic extends VariableMechanic {
    private final String axisVarName;

    public OrientVectorMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("axis");

        axisVarName = config.getString("axis");
    }

    @Override
    public void cast(SkillMetadata meta) {

        Variable targetVar = meta.getVariable(getVariableName());
        Validate.isTrue(targetVar instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        Vector target = ((PositionVariable) targetVar).getStored().toVector();

        Variable axisVar = meta.getVariable(axisVarName);
        Validate.isTrue(axisVar instanceof PositionVariable, "Variable '" + axisVarName + "' is not a vector");
        Vector axis = ((PositionVariable) axisVar).getStored().toVector();

        targetVar.setStored(new Position(meta.getSourceLocation().getWorld(), UtilityMethods.rotate(target, axis)));
    }
}
