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
public class SetYMechanic extends VariableMechanic {
    private final DoubleFormula coordinate;

    public SetYMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("y");

        coordinate = new DoubleFormula(config.getString("y"));
    }

    @Override
    public void cast(SkillMetadata meta) {

        Variable targetVar = meta.getVariable(getVariableName());
        Validate.isTrue(targetVar instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        Position target = (Position) targetVar.getStored();

        target.setY(coordinate.evaluate(meta));
    }
}
