package io.lumine.mythic.lib.script.mechanic.variable.vector;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Position;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

@MechanicMetadata
public class SetYMechanic extends VariableMechanic {
    private final NumericExpression coordinate;

    public SetYMechanic(ConfigObject config) {
        super(config);

        coordinate = config.numericExpr("value", "val", "v", "y");
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {

        var targetVar = meta.getVariable(getVariableName());
        Validate.isTrue(targetVar instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        Position target = (Position) targetVar.getStored();

        target.setY(coordinate.evaluate(meta));
    }
}
