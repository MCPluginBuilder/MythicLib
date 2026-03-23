package io.lumine.mythic.lib.script.mechanic.variable;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.script.variable.def.IntegerVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.jetbrains.annotations.NotNull;

@MechanicMetadata
public class SetIntegerMechanic extends VariableMechanic {
    private final NumericExpression formula;

    public SetIntegerMechanic(ConfigObject config) {
        super(config);

        formula = config.numericExpr("value");
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        getTargetVariableList(meta).registerVariable(new IntegerVariable(getVariableName(), (int) formula.evaluate(meta)));
    }
}
