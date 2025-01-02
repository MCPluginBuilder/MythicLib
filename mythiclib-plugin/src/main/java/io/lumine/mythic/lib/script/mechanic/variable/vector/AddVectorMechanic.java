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
public class AddVectorMechanic extends VariableMechanic {
    private final DoubleFormula x, y, z;
    private final String varToAdd;

    public AddVectorMechanic(ConfigObject config) {
        super(config);

        // Term by term addition
        x = config.contains("x") ? new DoubleFormula(config.getString("x")) : DoubleFormula.ZERO;
        y = config.contains("y") ? new DoubleFormula(config.getString("y")) : DoubleFormula.ZERO;
        z = config.contains("z") ? new DoubleFormula(config.getString("z")) : DoubleFormula.ZERO;

        // Vector addition
        varToAdd = config.getString("added", null);
    }

    @Override
    public void cast(SkillMetadata meta) {

        Variable targetVar = meta.getVariable(getVariableName());
        Validate.isTrue(targetVar instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        Position target = (Position) targetVar.getStored();

        // Vector addition
        if (varToAdd != null) {
            Variable var = meta.getVariable(varToAdd);
            Validate.isTrue(var instanceof PositionVariable, "Variable '" + varToAdd + "' is not a vector");
            target.add(((PositionVariable) var).getStored());
        }

        // Term by term addition
        double x = this.x.evaluate(meta);
        double y = this.y.evaluate(meta);
        double z = this.z.evaluate(meta);

        target.add(x, y, z);
    }
}
