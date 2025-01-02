package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Firefly extends SkillHandler<SimpleSkillResult> {
    public Firefly() {
        super();

        registerModifiers("damage", "duration", "knockback");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration") * 20;

        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            int j = 0;

            public void run() {
                if (j++ > duration)
                    cancel();

                if (caster.getLocation().getBlock().getType() == Material.WATER) {
                    caster.setVelocity(caster.getVelocity().multiply(3).setY(1.8));
                    caster.getWorld().playSound(caster.getLocation(), Sounds.BLOCK_FIRE_EXTINGUISH, 1, .5f);
                    caster.getWorld().spawnParticle(VParticle.EXPLOSION.get(), caster.getLocation().add(0, 1, 0), 32, 0, 0, 0, .2);
                    caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation().add(0, 1, 0), 32, 0, 0, 0, .2);
                    cancel();
                    return;
                }

                for (Entity entity : caster.getNearbyEntities(1, 1, 1))
                    if (UtilityMethods.canTarget(caster, entity)) {
                        double damage = skillMeta.getParameter("damage");
                        double knockback = skillMeta.getParameter("knockback");

                        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, .5f);
                        caster.getWorld().spawnParticle(Particle.LAVA, caster.getLocation().add(0, 1, 0), 32);
                        caster.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), caster.getLocation().add(0, 1, 0), 24, 0, 0, 0, .3);
                        caster.getWorld().spawnParticle(Particle.FLAME, caster.getLocation().add(0, 1, 0), 24, 0, 0, 0, .3);
                        entity.setVelocity(caster.getVelocity().setY(0.3).multiply(1.7 * knockback));
                        caster.setVelocity(caster.getEyeLocation().getDirection().multiply(-3).setY(.5));
                        skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);
                        cancel();
                        return;
                    }

                Location loc = caster.getLocation().add(0, 1, 0);
                for (double a = 0; a < Math.PI * 2; a += Math.PI / 9) {
                    Vector vec = new Vector(.6 * Math.cos(a), .6 * Math.sin(a), 0);
                    vec = UtilityMethods.rotate(vec, loc.getDirection());
                    loc.add(vec);
                    caster.getWorld().spawnParticle(VParticle.SMOKE.get(), loc, 0);
                    if (RANDOM.nextDouble() < .3)
                        caster.getWorld().spawnParticle(Particle.FLAME, loc, 0);
                    loc.add(vec.multiply(-1));
                }

                caster.setVelocity(caster.getEyeLocation().getDirection());
                caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
