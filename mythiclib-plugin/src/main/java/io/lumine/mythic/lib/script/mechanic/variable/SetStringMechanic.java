package io.lumine.mythic.lib.script.mechanic.variable;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.variable.def.StringVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.jetbrains.annotations.NotNull;

@MechanicMetadata
public class SetStringMechanic extends VariableMechanic {
    private final String value;

    public SetStringMechanic(ConfigObject config) {
        super(config);

        value = config.getString("value");
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        getTargetVariableList(meta).registerVariable(new StringVariable(getVariableName(), meta.parseString(value)));
    }
}
