package io.lumine.mythic.lib.script.mechanic.variable.vector;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.variable.VariableMechanic;
import io.lumine.mythic.lib.script.variable.def.PositionVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.Position;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

@MechanicMetadata
public class CrossProductMechanic extends VariableMechanic {
    private final String varName1, varName2;

    public CrossProductMechanic(ConfigObject config) {
        super(config);

        // Term by term addition
        varName1 = config.getString("vec1");
        varName2 = config.getString("vec2");
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {

        var var1 = meta.getVariable(varName1);
        Validate.isTrue(var1 instanceof PositionVariable, "Variable '" + varName1 + "' is not a vector");
        Position pos1 = (Position) var1.getStored();

        var var2 = meta.getVariable(varName2);
        Validate.isTrue(var2 instanceof PositionVariable, "Variable '" + varName2 + "' is not a vector");
        Position pos2 = (Position) var2.getStored();

        getTargetVariableList(meta).registerVariable(new PositionVariable(getVariableName(), pos1.getCrossProduct(pos2)));
    }
}
