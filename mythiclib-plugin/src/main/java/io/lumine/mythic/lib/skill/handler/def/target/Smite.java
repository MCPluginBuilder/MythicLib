package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"cooldown", "damage"})
public class Smite extends SkillHandler<TargetSkillResult> {
    public Smite(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        skillMeta.getCaster().attack(target, skillMeta.getParameter("damage"), DamageType.SKILL, DamageType.MAGIC);
        target.getWorld().strikeLightningEffect(target.getLocation());
    }
}
