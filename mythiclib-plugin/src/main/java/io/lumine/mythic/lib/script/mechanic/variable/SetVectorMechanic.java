package io.lumine.mythic.lib.script.mechanic.variable;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Position;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.jetbrains.annotations.NotNull;

@MechanicMetadata
public class SetVectorMechanic extends VariableMechanic {
    private final NumericExpression x, y, z;

    public SetVectorMechanic(ConfigObject config) {
        super(config);

        x = config.numericExpr(NumericExpression.ZERO, "x");
        y = config.numericExpr(NumericExpression.ZERO, "y");
        z = config.numericExpr(NumericExpression.ZERO, "z");
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        getTargetVariableList(meta).registerVariable(new PositionVariable(getVariableName(), new Position(meta.getSourceLocation().getWorld(), x.evaluate(meta), y.evaluate(meta), z.evaluate(meta))));
    }
}
