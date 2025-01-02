package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Freezing_Curse extends SkillHandler<LocationSkillResult> {
    public Freezing_Curse() {
        super();

        registerModifiers("cooldown", "duration", "damage", "radius", "amplifier");
    }

    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            final double rads = Math.toRadians(caster.getEyeLocation().getYaw() - 90);
            double ti = rads;
            int j = 0;

            public void run() {

                if (j++ % 2 == 0)
                    loc.getWorld().playSound(loc, Sounds.BLOCK_NOTE_BLOCK_PLING, 2, (float) (.5 + ((ti - rads) / (Math.PI * 2) * 1.5)));
                for (int j = 0; j < 2; j++) {
                    ti += Math.PI / 32;
                    loc.getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(), loc.clone().add(Math.cos(ti) * 3, .1, Math.sin(ti) * 3), 0);
                }

                if (ti > Math.PI * 2 + rads) {
                    loc.getWorld().playSound(loc, Sounds.BLOCK_GLASS_BREAK, 3, .5f);

                    for (double j = 0; j < Math.PI * 2; j += Math.PI / 32)
                        loc.getWorld().spawnParticle(Particle.CLOUD, loc.clone().add(Math.cos(j) * 3, .1, Math.sin(j) * 3), 0);

                    double radius = skillMeta.getParameter("radius");
                    double amplifier = skillMeta.getParameter("amplifier");
                    double duration = skillMeta.getParameter("duration");
                    double damage = skillMeta.getParameter("damage");
                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                        if (entity.getLocation().distanceSquared(loc) < radius * radius && UtilityMethods.canTarget(caster, entity)) {
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, DamageType.SKILL, DamageType.MAGIC);
                            UtilityMethods.forcePotionEffect((LivingEntity) entity, VPotionEffectType.SLOWNESS.get(), duration, (int) amplifier);
                        }
                    cancel();
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
