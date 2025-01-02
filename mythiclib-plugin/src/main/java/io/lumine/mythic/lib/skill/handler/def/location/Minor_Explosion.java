package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Minor_Explosion extends SkillHandler<LocationSkillResult> {
    public Minor_Explosion() {
        super();

        registerModifiers("damage", "knockback", "radius");
    }

    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double damage = skillMeta.getParameter("damage");
        double radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);
        double knockback = skillMeta.getParameter("knockback");

        loc.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), loc.add(0, .1, 0), 32, 1.7, 1.7, 1.7, 0);
        loc.getWorld().spawnParticle(VParticle.EXPLOSION.get(), loc, 64, 0, 0, 0, .3);
        loc.getWorld().playSound(loc, Sounds.ENTITY_GENERIC_EXPLODE, 2, 1);

        for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
            if (entity.getLocation().distanceSquared(loc) < radiusSquared && UtilityMethods.canTarget(caster, entity)) {
                skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);
                entity.setVelocity(UtilityMethods.safeNormalize(entity.getLocation().subtract(loc).toVector().setY(0)).setY(.2).multiply(2 * knockback));
            }
    }
}
