package io.lumine.mythic.lib.script.mechanic.variable;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.script.variable.def.DoubleVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.jetbrains.annotations.NotNull;

@MechanicMetadata
public class SetDoubleMechanic extends VariableMechanic {
    private final NumericExpression formula;

    public SetDoubleMechanic(ConfigObject config) {
        super(config);

        formula = config.numericExpr("value", "val", "double", "float", "rhs");
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        getTargetVariableList(meta).registerVariable(new DoubleVariable(getVariableName(), formula.evaluate(meta)));
    }
}
