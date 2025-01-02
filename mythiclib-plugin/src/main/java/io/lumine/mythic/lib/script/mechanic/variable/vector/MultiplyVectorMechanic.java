package io.lumine.mythic.lib.script.mechanic.variable.vector;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;

@MechanicMetadata
public class MultiplyVectorMechanic extends VariableMechanic {
    private final DoubleFormula coef;

    public MultiplyVectorMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("coef");

        coef = new DoubleFormula(config.getString("coef"));
    }

    @Override
    public void cast(SkillMetadata meta) {
        Variable var = meta.getVariable(getVariableName());
        Validate.isTrue(var instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        ((PositionVariable) var).getStored().multiply(coef.evaluate(meta));
    }
}
