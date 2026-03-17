package io.lumine.mythic.lib.script.util.expression.placeholder;

import io.lumine.mythic.lib.script.util.expression.EvaluationException;
import io.lumine.mythic.lib.script.variable.def.DoubleVariable;
import io.lumine.mythic.lib.script.variable.def.IntegerVariable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.jetbrains.annotations.NotNull;

public class MythicLibVariablePlaceholder implements ExpressionPlaceholder {
    private final String fullVariableName;

    public MythicLibVariablePlaceholder(String fullVariableName) {
        this.fullVariableName = fullVariableName;
    }

    @Override
    public Double parse(@NotNull SkillMetadata skillMetadata) {
        final var result = skillMetadata.getVariable(fullVariableName);
        if (result instanceof IntegerVariable) return (double) ((IntegerVariable) result).getStored();
        if (result instanceof DoubleVariable) return ((DoubleVariable) result).getStored();
        throw new EvaluationException("Variable " + fullVariableName + " did not evaluate to a numerical value, got " + result.getClass().getSimpleName());
    }
}
