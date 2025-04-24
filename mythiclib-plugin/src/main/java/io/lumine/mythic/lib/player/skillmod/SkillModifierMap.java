package io.lumine.mythic.lib.player.skillmod;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.modifier.ModifierMap;
import io.lumine.mythic.lib.player.modifier.ModifierType;
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

        // Flat
        for (SkillModifier mod : getModifiers())
            if (mod.getType() == ModifierType.FLAT
                    && mod.getParameter().equals(parameter)
                    && mod.getSkills().contains(skill))
                base += mod.getValue();

        // Additive scalars
        double scalar = 1;
        for (SkillModifier mod : getModifiers())
            if (mod.getType() == ModifierType.ADDITIVE_MULTIPLIER
                    && mod.getParameter().equals(parameter)
                    && mod.getSkills().contains(skill))
                scalar += mod.getValue() / 100;
        base *= scalar;

        // Multiplicative scalars
        for (SkillModifier mod : getModifiers())
            if (mod.getType() == ModifierType.RELATIVE
                    && mod.getParameter().equals(parameter)
                    && mod.getSkills().contains(skill))
                base *= 1 + mod.getValue() / 100;

        return base;
    }
}
