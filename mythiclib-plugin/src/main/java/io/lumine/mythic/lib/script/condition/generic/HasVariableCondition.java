package io.lumine.mythic.lib.script.condition.generic;

import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Checks if two strings are equal
 */
public class HasVariableCondition extends Condition {
    private final String variableName;

    public HasVariableCondition(ConfigObject config) {
        super(config);

        variableName = config.string("variable", "var", "v", "name", "n");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        try {
            meta.getVariable(variableName);
            return true;
        } catch (Exception exception) {
            // Need to catch all possible exceptions
            // VariableNotFound for custom variables
            // NullPointerExceptions and IllegalArgumentExceptions for reserved variables
            return false;
        }
    }
}
