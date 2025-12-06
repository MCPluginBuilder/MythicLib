package io.lumine.mythic.lib.script.util.expression.placeholder;

import io.lumine.mythic.lib.skill.SkillMetadata;
import org.jetbrains.annotations.NotNull;

public interface ExpressionPlaceholder {

    public Double parse(@NotNull SkillMetadata skillMetadata);
}
