package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Fire_Meteor extends SkillHandler<VectorSkillResult> {
    public Fire_Meteor() {
        super();

        registerModifiers("damage", "knockback", "radius");
    }

    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 3, 1);
        new BukkitRunnable() {
            final Location loc = caster.getLocation().clone().add(0, 10, 0);
            final Vector vec = result.getTarget().multiply(1.3).setY(-1).normalize();
            double ti = 0;

            public void run() {
                ti++;
                if (ti > 40)
                    cancel();

                loc.add(vec);
                loc.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), loc, 0);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, .2, .2, .2, 0);
                if (loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid() || loc.getBlock().getType().isSolid()) {
                    loc.getWorld().playSound(loc, Sounds.ENTITY_GENERIC_EXPLODE, 3, .6f);
                    loc.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), loc, 10, 2, 2, 2, 0);
                    loc.getWorld().spawnParticle(VParticle.EXPLOSION.get(), loc, 32, 0, 0, 0, .3);
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .3);

                    double damage = skillMeta.getParameter("damage");
                    double knockback = skillMeta.getParameter("knockback");
                    double radius = skillMeta.getParameter("radius");
                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                        if (UtilityMethods.canTarget(caster, entity) && entity.getLocation().distanceSquared(loc) < radius * radius) {
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE);
                            entity.setVelocity(entity.getLocation().toVector().subtract(loc.toVector()).multiply(.1 * knockback).setY(.4 * knockback));
                        }
                    cancel();
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
