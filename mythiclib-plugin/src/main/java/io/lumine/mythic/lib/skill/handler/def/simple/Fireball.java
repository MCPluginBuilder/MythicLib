package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.CustomProjectileHandler;
import io.lumine.mythic.lib.util.RayTrace;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Fireball extends SkillHandler<SimpleSkillResult> {
    public Fireball() {
        super();

        registerModifiers("damage", "ignite", "ratio");
    }

    @NotNull
    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
        new BukkitRunnable() {
            int j = 0;
            final Vector vec = caster.getPlayer().getEyeLocation().getDirection();
            final Location loc = caster.getPlayer().getLocation().add(0, 1.3, 0);
            final CustomProjectileHandler proj = new CustomProjectileHandler(skillMeta.getCaster(), InteractionType.OFFENSE_SKILL);

            public void run() {
                if (j++ > 40) {
                    cancel();
                    return;
                }

                loc.add(vec);

                if (j % 3 == 0)
                    loc.getWorld().playSound(loc, Sounds.BLOCK_FIRE_AMBIENT, 2, 1);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, .02, .02, .02, 0);
                loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);

                final @Nullable LivingEntity target = proj.findTarget(loc);
                if (target != null) {
                    loc.getWorld().spawnParticle(Particle.LAVA, loc, 8);
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .1);
                    loc.getWorld().playSound(loc, Sounds.ENTITY_BLAZE_HURT, 2, 1);
                    target.setFireTicks((int) (target.getFireTicks() + skillMeta.getParameter("ignite") * 20));
                    double damage = skillMeta.getParameter("damage");
                    skillMeta.getCaster().attack((LivingEntity) target, damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE);

                    new BukkitRunnable() {
                        int i = 0;

                        @Override
                        public void run() {
                            if (i++ > 2) {
                                cancel();
                                return;
                            }

                            double range = 2.5 * (1 + RANDOM.nextDouble());
                            Vector dir = randomDirection();
                            loc.getWorld().playSound(loc, Sounds.ENTITY_BLAZE_HURT, 2, 1.5f);

                            RayTrace result = new RayTrace(loc, dir, range, entity -> UtilityMethods.canTarget(caster, entity));
                            if (result.hasHit())
                                skillMeta.getCaster().attack(result.getHit(), damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE);
                            result.draw(.13, tick -> tick.getWorld().spawnParticle(Particle.FLAME, tick, 0));
                        }
                    }.runTaskTimer(MythicLib.plugin, 3, 3);

                    cancel();
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }

    private Vector randomDirection() {
        double x = RANDOM.nextDouble() - .5, y = (RANDOM.nextDouble() - .2) / 2, z = RANDOM.nextDouble() - .5;
        Vector dir = new Vector(x, y, z);
        return dir.lengthSquared() == 0 ? new Vector(1, 0, 0) : dir.normalize();
    }
}
