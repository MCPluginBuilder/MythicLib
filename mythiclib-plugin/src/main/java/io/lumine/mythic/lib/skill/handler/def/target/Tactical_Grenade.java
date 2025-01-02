package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Tactical_Grenade extends SkillHandler<TargetSkillResult> {
    public Tactical_Grenade() {
        super();

        registerModifiers("knock-up", "damage", "radius");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            final Location loc = caster.getLocation().add(0, .1, 0);
            final double radius = skillMeta.getParameter("radius");
            final double knockup = .7 * skillMeta.getParameter("knock-up");
            final List<Integer> hit = new ArrayList<>();
            int j = 0;

            public void run() {
                if (target.isDead() || !target.getWorld().equals(loc.getWorld()) || j++ > 200) {
                    cancel();
                    return;
                }

                Vector vec = target.getLocation().add(0, .1, 0).subtract(loc).toVector();
                vec = vec.length() < 3 ? vec : vec.normalize().multiply(3);
                loc.add(vec);

                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 32, 1, 0, 1, 0);
                loc.getWorld().spawnParticle(VParticle.EXPLOSION.get(), loc, 16, 1, 0, 1, .05);
                loc.getWorld().playSound(loc, Sounds.BLOCK_ANVIL_LAND, 2, 0);
                loc.getWorld().playSound(loc, Sounds.ENTITY_GENERIC_EXPLODE, 2, 1);

                for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                    if (!hit.contains(entity.getEntityId()) && UtilityMethods.canTarget(caster, entity) && entity.getLocation().distanceSquared(loc) < radius * radius) {

                        /*
                         * Stop the runnable as soon as the
                         * grenade finally hits the initial target.
                         */
                        hit.add(entity.getEntityId());
                        if (entity.equals(target))
                            cancel();

                        skillMeta.getCaster().attack((LivingEntity) entity, skillMeta.getParameter("damage"), DamageType.SKILL, DamageType.MAGIC);
                        entity.setVelocity(entity.getVelocity().add(offsetVector(knockup)));
                    }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 12);
    }

    private Vector offsetVector(double y) {
        return new Vector(2 * (RANDOM.nextDouble() - .5), y, 2 * (RANDOM.nextDouble() - .5));
    }
}