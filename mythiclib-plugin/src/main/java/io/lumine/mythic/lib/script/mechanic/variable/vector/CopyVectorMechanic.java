package io.lumine.mythic.lib.script.mechanic.variable.vector;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;

/**
 * Copies a given vector and saves it into a
 * custom variable which can later be modified
 */
@MechanicMetadata
public class CopyVectorMechanic extends VariableMechanic {
    private final String varName;

    public CopyVectorMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("value");

        varName = config.getString("value");
    }

    @Override
    public void cast(SkillMetadata meta) {
        Variable var = meta.getVariable(varName);
        Validate.isTrue(var instanceof PositionVariable, "Variable '" + var.getName() + "' is not a vector");
        getTargetVariableList(meta).registerVariable(new PositionVariable(getVariableName(), ((PositionVariable) var).getStored().clone()));
    }
}
