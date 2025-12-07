package io.lumine.mythic.lib.script.util.expression.placeholder;

import io.lumine.mythic.lib.skill.SkillMetadata;
import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;

public class PAPIPlaceholder implements ExpressionPlaceholder {
    private final String placeholderName;

    public PAPIPlaceholder(String placeholderName) {
        this.placeholderName = "{" + placeholderName + "}";
    }

    @Override
    public Double parse(@NotNull SkillMetadata skillMetadata) {
        // Using {..} is arbitrary, could use %..% instead
        final var parsed = PlaceholderAPI.setBracketPlaceholders(skillMetadata.getCaster().getPlayer(), this.placeholderName);
        return Double.parseDouble(parsed);
    }
}
