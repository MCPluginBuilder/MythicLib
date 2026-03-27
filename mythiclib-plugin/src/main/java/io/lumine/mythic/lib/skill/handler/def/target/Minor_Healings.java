package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.player.resource.Resources;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.SmallParticleEffect;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"heal"})
public class Minor_Healings extends SkillHandler<TargetSkillResult> {
    public Minor_Healings(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return meta.getCaster().getPlayer().isSneaking() ? new TargetSkillResult(meta.getCaster().getPlayer()) : new TargetSkillResult(meta, InteractionType.SUPPORT_SKILL);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Resources.heal(target, skillMeta.getParameter("heal"));
        new SmallParticleEffect(target, Particle.HEART, 1);
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_PLAYER_LEVELUP, 2, 2);
    }
}
