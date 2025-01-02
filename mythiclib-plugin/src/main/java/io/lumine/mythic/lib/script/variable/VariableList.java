package io.lumine.mythic.lib.script.variable;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class VariableList implements VariableContainer {
    private final VariableScope scope;
    private final Map<String, Variable> vars = new HashMap<>();

    public VariableList(@NotNull VariableScope scope) {
        this.scope = scope;
    }

    @NotNull
    public VariableScope getScope() {
        return scope;
    }

    @Nullable
    public Variable getVariable(@NotNull String name) {
        return vars.get(name);
    }

    public void registerVariable(@NotNull Variable var) {
        Validate.isTrue(!SkillMetadata.RESERVED_VARIABLE_NAMES.contains(var.getName()), "Cannot use reserved variable name '" + var.getName() + "'");
        vars.put(var.getName(), var);
    }
}
