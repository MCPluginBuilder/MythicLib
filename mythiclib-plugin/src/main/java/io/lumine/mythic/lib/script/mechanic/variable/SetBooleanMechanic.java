package io.lumine.mythic.lib.script.mechanic.variable;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.variable.def.BooleanVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.formula.BooleanExpression;
import org.jetbrains.annotations.NotNull;

@MechanicMetadata
public class SetBooleanMechanic extends VariableMechanic {
    private final String expression;

    public SetBooleanMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("value");

        expression = config.getString("value");
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        final boolean value = BooleanExpression.eval(meta.parseString(expression));
        getTargetVariableList(meta).registerVariable(new BooleanVariable(getVariableName(), value));
    }
}
