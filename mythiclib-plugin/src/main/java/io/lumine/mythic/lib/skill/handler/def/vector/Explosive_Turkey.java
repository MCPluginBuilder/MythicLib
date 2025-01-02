package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Explosive_Turkey extends SkillHandler<VectorSkillResult> {
    public Explosive_Turkey() {
        super();

        registerModifiers("damage", "radius", "duration", "knockback");
    }

    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        double duration = skillMeta.getParameter("duration") * 10;
        double damage = skillMeta.getParameter("damage");
        double radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);
        double knockback = skillMeta.getParameter("knockback");

        Vector vec = result.getTarget().normalize().multiply(.6);

        Chicken chicken = (Chicken) caster.getWorld().spawnEntity(caster.getLocation().add(0, 1.3, 0).add(vec),
                EntityType.CHICKEN);
        ChickenHandler chickenHandler = new ChickenHandler(chicken);
        chicken.setInvulnerable(true);
        chicken.setSilent(true);

        /*
         * Sets the health to 2048 (Default max Spigot value) which stops the
         * bug where you can kill the chicken for a brief few ticks after it
         * spawns in!
         */
        chicken.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2048);
        chicken.setHealth(2048);

        /*
         * When items are moving through the air, they loose a percent of their
         * velocity proportionally to their coordinates in each axis. This means
         * that if the trajectory is not affected, the ratio of x/y will always
         * be the same. Check for any change of that ratio to check for a
         * trajectory change
         */
        chicken.setVelocity(vec);

        final double trajRatio = chicken.getVelocity().getX() / chicken.getVelocity().getZ();

        new BukkitRunnable() {
            int ti = 0;

            public void run() {
                if (ti++ > duration || chicken.isDead()) {
                    chickenHandler.close();
                    cancel();
                    return;
                }

                chicken.setVelocity(vec);
                if (ti % 4 == 0)
                    chicken.getWorld().playSound(chicken.getLocation(), Sounds.ENTITY_CHICKEN_HURT, 2, 1);
                chicken.getWorld().spawnParticle(VParticle.EXPLOSION.get(), chicken.getLocation().add(0, .3, 0), 0);
                chicken.getWorld().spawnParticle(VParticle.FIREWORK.get(), chicken.getLocation().add(0, .3, 0), 1, 0, 0, 0, .05);
                double currentTrajRatio = chicken.getVelocity().getX() / chicken.getVelocity().getZ();
                if (chicken.isOnGround() || Math.abs(trajRatio - currentTrajRatio) > .1) {

                    chickenHandler.close();
                    cancel();

                    chicken.getWorld().spawnParticle(VParticle.FIREWORK.get(), chicken.getLocation().add(0, .3, 0), 128, 0, 0, 0, .25);
                    chicken.getWorld().spawnParticle(VParticle.EXPLOSION.get(), chicken.getLocation().add(0, .3, 0), 24, 0, 0, 0, .25);
                    chicken.getWorld().playSound(chicken.getLocation(), Sounds.ENTITY_GENERIC_EXPLODE, 2, 1.5f);
                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(chicken.getLocation()))
                        if (!entity.isDead() && entity.getLocation().distanceSquared(chicken.getLocation()) < radiusSquared
                                && UtilityMethods.canTarget(caster, entity)) {
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE);
                            entity.setVelocity(entity.getLocation().toVector().subtract(chicken.getLocation().toVector()).multiply(.1 * knockback)
                                    .setY(.4 * knockback));
                        }
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }

    /**
     * This fixes an issue where chickens sometimes drop
     */
    public static class ChickenHandler extends TemporaryListener {
        private final Chicken chicken;

        public ChickenHandler(Chicken chicken) {
            this.chicken = chicken;
        }

        @Override
        public void whenClosed() {
            chicken.remove();
        }

        @EventHandler
        public void a(EntityDeathEvent event) {
            if (event.getEntity().equals(chicken)) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
        }
    }
}
