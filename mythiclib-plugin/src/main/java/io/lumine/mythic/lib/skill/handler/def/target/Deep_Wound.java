package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class Deep_Wound extends SkillHandler<TargetSkillResult> {
    public Deep_Wound() {
        super();

        registerModifiers("damage", "extra");
    }

    @NotNull
    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 2);
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, target.getHeight() / 2, 0), 32, 0, 0, 0, .7);
        target.getWorld().spawnParticle(VParticle.BLOCK.get(), target.getLocation().add(0, target.getHeight() / 2, 0), 32, 0, 0, 0, 2,
                Material.REDSTONE_BLOCK.createBlockData());

        final double max = target.getAttribute(Attributes.MAX_HEALTH).getValue();
        final double ratio = (max - target.getHealth()) / max;
        final double damage = skillMeta.getParameter("damage") * (1 + skillMeta.getParameter("extra") * ratio / 100);
        skillMeta.getCaster().attack(target, damage, DamageType.SKILL, DamageType.PHYSICAL);
    }
}
