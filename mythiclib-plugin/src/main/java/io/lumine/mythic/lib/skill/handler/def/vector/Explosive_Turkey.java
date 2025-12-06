package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "radius", "duration", "knockback"})
public class Explosive_Turkey extends SkillHandler<VectorSkillResult> {
    private final List<DamageType> damageTypes;

    public Explosive_Turkey(ConfigurationSection config) {
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

        final var vec = result.getTarget().normalize().multiply(.6);
        final var chicken = (Chicken) caster.getWorld().spawnEntity(caster.getLocation().add(0, 1.3, 0).add(vec), EntityType.CHICKEN);
        chicken.setInvulnerable(true);
        chicken.setSilent(true);
        new Handler(chicken, vec, skillMeta);

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
    }

    /**
     * This fixes an issue where chickens sometimes drop
     */
    class Handler extends TemporaryHandler {
        private final Chicken chicken;
        private final Vector vec;
        private final PlayerMetadata caster;

        private final double duration, damage, radiusSquared, knockback, trajRatio;

        public Handler(Chicken chicken, Vector vec, SkillMetadata skillMeta) {
            super(skillMeta.getCaster().getData());

            this.chicken = chicken;
            this.vec = vec;
            this.caster = skillMeta.getCaster();

            this.duration = skillMeta.getParameter("duration") * 10;
            this.damage = skillMeta.getParameter("damage");
            this.radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);
            this.knockback = skillMeta.getParameter("knockback");
            this.trajRatio = chicken.getVelocity().getX() / chicken.getVelocity().getZ();

            runTask(runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        @Override
        protected BukkitRunnable newTask() {
            return new BukkitRunnable() {
                int ti = 0;

                public void run() {
                    if (ti++ > duration || chicken.isDead()) {
                        Handler.this.close();
                        return;
                    }

                    chicken.setVelocity(vec);
                    if (ti % 4 == 0)
                        chicken.getWorld().playSound(chicken.getLocation(), Sounds.ENTITY_CHICKEN_HURT, 2, 1);
                    chicken.getWorld().spawnParticle(VParticle.EXPLOSION.get(), chicken.getLocation().add(0, .3, 0), 0);
                    chicken.getWorld().spawnParticle(VParticle.FIREWORK.get(), chicken.getLocation().add(0, .3, 0), 1, 0, 0, 0, .05);
                    double currentTrajRatio = chicken.getVelocity().getX() / chicken.getVelocity().getZ();
                    if (chicken.isOnGround() || Math.abs(trajRatio - currentTrajRatio) > .1) {

                        Handler.this.close();

                        chicken.getWorld().spawnParticle(VParticle.FIREWORK.get(), chicken.getLocation().add(0, .3, 0), 128, 0, 0, 0, .25);
                        chicken.getWorld().spawnParticle(VParticle.EXPLOSION.get(), chicken.getLocation().add(0, .3, 0), 24, 0, 0, 0, .25);
                        chicken.getWorld().playSound(chicken.getLocation(), Sounds.ENTITY_GENERIC_EXPLODE, 2, 1.5f);
                        for (Entity entity : UtilityMethods.getNearbyChunkEntities(chicken.getLocation()))
                            if (!entity.isDead() && entity.getLocation().distanceSquared(chicken.getLocation()) < radiusSquared
                                    && UtilityMethods.canTarget(caster.getPlayer(), entity)) {
                                caster.attack((LivingEntity) entity, damage, damageTypes);
                                entity.setVelocity(entity.getLocation().toVector().subtract(chicken.getLocation().toVector()).multiply(.1 * knockback)
                                        .setY(.4 * knockback));
                            }
                    }
                }
            };
        }

        @Override
        public void onClose() {
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
