package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "knockback", "radius"})
public class Fire_Meteor extends SkillHandler<VectorSkillResult> {
    private final List<DamageType> damageTypes;

    public Fire_Meteor(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), config.get("damage_types"));
    }

    @Override
    public @NotNull VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 3, 1);
        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final Location loc = caster.getLocation().clone().add(0, 10, 0);
            final Vector vec = result.getTarget().multiply(1.3).setY(-1).normalize();
            double ti = 0;

            public void run() {
                if (++ti > 40) {
                    handler.close();
                    return;
                }

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
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, damageTypes);
                            entity.setVelocity(entity.getLocation().toVector().subtract(loc.toVector()).multiply(.1 * knockback).setY(.4 * knockback));
                        }

                    handler.close();
                }
            }
        });
    }
}
