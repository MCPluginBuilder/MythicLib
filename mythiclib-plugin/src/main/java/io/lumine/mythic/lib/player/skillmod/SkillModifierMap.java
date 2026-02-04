package io.lumine.mythic.lib.player.skillmod;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierMap;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import org.jetbrains.annotations.NotNull;

public class SkillModifierMap extends ModifierMap<SkillModifier> {
    public SkillModifierMap(MMOPlayerData playerData) {
        super(playerData);
    }

    public double calculateValue(@NotNull Skill cast, @NotNull String parameter) {
        return calculateValue(cast.getHandler(), cast.getParameter(parameter), parameter);
    }

    public double calculateValue(@NotNull SkillHandler<?> skill, double base, @NotNull String parameter) {

        // Allows for independent iterations for max parallelism
        var addScalar = 1d;
        var multScalar = 1d;

        for (var mod : modifiers.values())
            if (mod.getSkills().contains(skill) && mod.getParameter().equals(parameter))
                switch (mod.getType()) {

                    case FLAT:
                        // Flat modifiers
                        base += mod.getValue();
                        continue;

                    case RELATIVE:
                        // Additive scalars
                        addScalar += mod.getValue() / 100;
                        continue;

                    case ADDITIVE_MULTIPLIER:
                        // Multiplicative/Compound scalars
                        // Bad naming
                        multScalar *= 1 + (mod.getValue() / 100);
                }

        return base * addScalar * multScalar;
    }
}
