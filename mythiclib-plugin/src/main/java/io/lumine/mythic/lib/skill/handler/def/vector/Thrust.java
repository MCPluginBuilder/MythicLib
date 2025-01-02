package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public class Thrust extends SkillHandler<VectorSkillResult> {
    public Thrust() {
        super();

        registerModifiers("damage");
    }

    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        final Player caster = skillMeta.getCaster().getPlayer();
        final double damage = skillMeta.getParameter("damage");

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 0);
        caster.addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), 2, 3));

        Location loc = caster.getEyeLocation().clone();
        Vector vec = result.getTarget().multiply(.5);
        for (double j = 0; j < 7; j += .5) {
            loc.add(vec);
            for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                if (UtilityMethods.canTarget(caster, loc, entity))
                    skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.PHYSICAL);
            loc.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), loc, 0);
        }
    }
}
