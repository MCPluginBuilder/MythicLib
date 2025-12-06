package io.lumine.mythic.lib.script.mechanic.variable.vector;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

@MechanicMetadata
public class MultiplyVectorMechanic extends VariableMechanic {
    private final NumericExpression coef;

    public MultiplyVectorMechanic(ConfigObject config) {
        super(config);

        coef = config.numericExpr("value", "val", "coefficient", "coef", "c", "scalar", "s");
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        var var = meta.getVariable(getVariableName());
        Validate.isTrue(var instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        ((PositionVariable) var).getStored().multiply(coef.evaluate(meta));
    }
}
