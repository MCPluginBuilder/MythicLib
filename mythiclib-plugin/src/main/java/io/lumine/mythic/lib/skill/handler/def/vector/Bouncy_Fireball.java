package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
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

public class Bouncy_Fireball extends SkillHandler<VectorSkillResult> {
    public Bouncy_Fireball() {
        super();

        registerModifiers("damage", "ignite", "speed", "radius");
    }

    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_SNOWBALL_THROW, 2, 0);
        new BukkitRunnable() {
            final Vector vec = result.getTarget().setY(0).normalize().multiply(.5 * skillMeta.getParameter("speed"));
            final Location loc = caster.getLocation().clone().add(0, 1.2, 0);
            int j = 0;
            int bounces = 0;

            double y = .3;

            public void run() {
                if (j++ > 100) {
                    loc.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), loc, 32, 0, 0, 0, .05);
                    loc.getWorld().playSound(loc, Sounds.BLOCK_FIRE_EXTINGUISH, 1, 1);
                    cancel();
                    return;
                }

                loc.add(vec);
                loc.add(0, y, 0);
                if (y > -.6)
                    y -= .05;

                loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, 0, 0, 0, .03);
                loc.getWorld().spawnParticle(VParticle.SMOKE.get(), loc, 1, 0, 0, 0, .03);

                if (loc.getBlock().getType().isSolid()) {
                    loc.add(0, -y, 0);
                    loc.add(vec.clone().multiply(-1));
                    y = .4;
                    bounces++;
                    loc.getWorld().playSound(loc, Sounds.ENTITY_BLAZE_HURT, 3, 2);
                }

                if (bounces > 2) {
                    double radius = skillMeta.getParameter("radius");
                    double damage = skillMeta.getParameter("damage");
                    double ignite = skillMeta.getParameter("ignite");

                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                        if (entity.getLocation().distanceSquared(loc) < radius * radius)
                            if (UtilityMethods.canTarget(caster, entity)) {
                                skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE);
                                entity.setFireTicks((int) (ignite * 20));
                            }

                    loc.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), loc, 12, 2, 2, 2, 0);
                    loc.getWorld().spawnParticle(VParticle.EXPLOSION.get(), loc, 48, 0, 0, 0, .2);
                    loc.getWorld().playSound(loc, Sounds.ENTITY_GENERIC_EXPLODE, 3, 0);
                    cancel();
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
