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
public class SubtractVectorMechanic extends VariableMechanic {
    private final NumericExpression x, y, z;
    private final String varToSubstract;

    public SubtractVectorMechanic(ConfigObject config) {
        super(config);

        // Term by term addition
        x = config.numericExpr((NumericExpression) null, "x");
        y = config.numericExpr((NumericExpression) null, "y");
        z = config.numericExpr((NumericExpression) null, "z");

        // Vector addition
        varToSubstract = config.stringFb(null, "subtracted", "subtract", "sub", "other", "rhs", "value", "val", "v");

        Validate.isTrue(varToSubstract != null || x != null || y != null || z != null, "Must provide at least one of 'x', 'y', 'z' or 'subtracted'");
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {

        var targetVar = meta.getVariable(getVariableName());
        Validate.isTrue(targetVar instanceof PositionVariable, "Variable '" + getVariableName() + "' is not a vector");
        Position target = (Position) targetVar.getStored();

        // Vector addition
        if (varToSubstract != null) {
            var var = meta.getVariable(varToSubstract);
            Validate.isTrue(var instanceof PositionVariable, "Variable '" + varToSubstract + "' is not a vector");
            target.add(((PositionVariable) var).getStored().clone().multiply(-1));
        }

        // Term by term addition
        double x = this.x == null ? 0 : this.x.evaluate(meta);
        double y = this.y == null ? 0 : this.y.evaluate(meta);
        double z = this.z == null ? 0 : this.z.evaluate(meta);

        target.add(-x, -y, -z);
    }
}
