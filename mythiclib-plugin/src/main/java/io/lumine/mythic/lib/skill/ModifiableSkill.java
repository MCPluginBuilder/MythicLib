package io.lumine.mythic.lib.skill;

import io.lumine.mythic.lib.skill.handler.SkillHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Can be used to cast a skill handler with configurable modifier input.
 *
 * @author jules
 */
public class ModifiableSkill extends Skill {
    private final Map<String, Double> modifiers = new HashMap<>();

    public ModifiableSkill(@NotNull SkillHandler<?> handler) {
        super(handler);
    }

    @Override
    public boolean getResult(@NotNull SkillMetadata skillMeta) {
        return true;
    }

    @Override
    public void whenCast(@NotNull SkillMetadata skillMeta) {
        // Nothing here
    }

    @Override
    public double getParameter(String path) {
        return modifiers.getOrDefault(path, 0d);
    }

    public void registerModifier(String path, double value) {
        modifiers.put(path, value);
    }
}
