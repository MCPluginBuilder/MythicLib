package io.lumine.mythic.lib.script.mechanic.variable;

import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.jetbrains.annotations.NotNull;

/**
 * Mechanic that affects a new value to a variable. It is
 * possible to choose what scope to set for the modified variable
 */
public abstract class VariableMechanic extends Mechanic {
    private final String varName;
    private final VariableScope scope;

    public VariableMechanic(ConfigObject config) {
        varName = config.string("variable", "var", "v");
        scope = config.parse(VariableScope.SKILL, Parsers.VARIABLE_SCOPE, "scope");
    }

    public String getVariableName() {
        return varName;
    }

    @NotNull
    public VariableList getTargetVariableList(SkillMetadata meta) {
        switch (this.scope) {
            case SERVER:
                return VariableList.SERVER;
            case PLAYER:
                return meta.getCaster().getData().getVariableList();
            case PROFILE:
                return meta.getCaster().getData().getProfileSession().getVariableList();
            case SKILL:
                return meta.getVariableList();
            default:
                throw new IllegalStateException("Unknown variable scope " + this.scope);
        }
    }
}
