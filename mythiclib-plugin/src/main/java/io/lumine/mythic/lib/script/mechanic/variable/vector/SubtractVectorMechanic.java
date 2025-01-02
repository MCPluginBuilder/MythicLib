package io.lumine.mythic.lib.script.mechanic.variable.vector;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.Position;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;

@MechanicMetadata
public class SubtractVectorMechanic extends VariableMechanic {
    private final DoubleFormula x, y, z;
    private final String varToSubstract;

    public SubtractVectorMechanic(ConfigObject config) {
        super(config);

        // Term by term addition
        x = config.contains("x") ? new DoubleFormula(config.getString("x")) : DoubleFormula.ZERO;
        y = config.contains("y") ? new DoubleFormula(config.getString("y")) : DoubleFormula.ZERO;
        z = config.contains("z") ? new DoubleFormula(config.getString("z")) : DoubleFormula.ZERO;

        // Vector addition
        varToSubstract = config.getString("subtracted", null);
    }

    @Override
    public void cast(SkillMetadata meta) {

        Variable targetVar = meta.getVariable(getVariableName());
        Validate.isTrue(targetVar instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        Position target = (Position) targetVar.getStored();

        // Vector addition
        if (varToSubstract != null) {
            Variable var = meta.getVariable(varToSubstract);
            Validate.isTrue(var instanceof PositionVariable, "Variable '" + varToSubstract + "' is not a vector");
            target.add(((PositionVariable) var).getStored().clone().multiply(-1));
        }

        // Term by term addition
        double x = this.x.evaluate(meta);
        double y = this.y.evaluate(meta);
        double z = this.z.evaluate(meta);

        target.add(-x, -y, -z);
    }
}
