package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Grand_Heal extends SkillHandler<SimpleSkillResult> {
    public Grand_Heal() {
        super();

        registerModifiers("heal", "radius");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        final double heal = skillMeta.getParameter("heal");
        final double radius = skillMeta.getParameter("radius");

        final Player caster = skillMeta.getCaster().getPlayer();
        UtilityMethods.heal(caster, heal);

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_PLAYER_LEVELUP, 1, 2);
        caster.getWorld().spawnParticle(Particle.HEART, caster.getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        caster.getWorld().spawnParticle(VParticle.HAPPY_VILLAGER.get(), caster.getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        for (Entity entity : caster.getNearbyEntities(radius, radius, radius))
            if (UtilityMethods.canTarget(caster, entity, InteractionType.SUPPORT_SKILL))
                UtilityMethods.heal((LivingEntity) entity, heal);
    }
}
