package io.lumine.mythic.lib.script.mechanic.variable;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Mechanic that affects a new value to a variable. It is
 * possible to choose what scope to set for the modified variable
 */
public abstract class VariableMechanic extends Mechanic {
    private final String varName;
    private final VariableScope scope;

    public VariableMechanic(ConfigObject config) {
        varName = config.string("variable", "var", "v");
        scope = config.contains("scope") ? UtilityMethods.prettyValueOf(VariableScope::valueOf, config.getString("scope"), "No scope with ID %s") : VariableScope.SKILL;
    }

    public String getVariableName() {
        return varName;
    }

    public VariableList getTargetVariableList(SkillMetadata meta) {
        return scope == VariableScope.SKILL ? meta.getVariableList() : meta.getCaster().getData().getVariableList();
    }
}
