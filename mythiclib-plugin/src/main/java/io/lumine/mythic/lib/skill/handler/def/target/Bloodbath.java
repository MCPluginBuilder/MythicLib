package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"amount"})
public class Bloodbath extends SkillHandler<TargetSkillResult> {
    public Bloodbath(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        var target = result.getTarget();
        var caster = skillMeta.getCaster().getPlayer();

        final var foodStolen = skillMeta.getParameter("amount");

        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_COW_HURT, 1, 2);
        target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.STEP_SOUND, 152);

        // Give food to caster
        caster.setFoodLevel((int) UtilityMethods.clamp(caster.getFoodLevel() + foodStolen, 0, 20));

        // Steal food only from players
        if (target instanceof Player) {
            ((Player) target).setFoodLevel((int) UtilityMethods.clamp(((Player) target).getFoodLevel() - foodStolen, 0, 20));
        }
    }
}
