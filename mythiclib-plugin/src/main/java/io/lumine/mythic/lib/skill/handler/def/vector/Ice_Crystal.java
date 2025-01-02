package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Ice_Crystal extends SkillHandler<VectorSkillResult> {
    public Ice_Crystal() {
        super();

        registerModifiers("damage", "duration", "amplifier");
    }

    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
        new BukkitRunnable() {
            final Vector vec = result.getTarget().multiply(.45);
            final Location loc = caster.getEyeLocation().clone().add(0, -.3, 0);
            int ti = 0;

            public void run() {
                if (ti++ > 25)
                    cancel();

                loc.getWorld().playSound(loc, Sounds.BLOCK_GLASS_BREAK, 2, 1);
                List<Entity> entities = UtilityMethods.getNearbyChunkEntities(loc);
                for (int j = 0; j < 3; j++) {
                    loc.add(vec);
                    if (loc.getBlock().getType().isSolid())
                        cancel();

                    /*
                     * has a different particle effect since SNOW_DIG is not the
                     * same as in legacy minecraft, the particle effect is now a
                     * cross that rotates
                     */
                    for (double r = 0; r < .4; r += .1)
                        for (double a = 0; a < Math.PI * 2; a += Math.PI / 2) {
                            Vector vec = UtilityMethods.rotate(new Vector(r * Math.cos(a + (double) ti / 10), r * Math.sin(a + (double) ti / 10), 0),
                                    loc.getDirection());
                            loc.add(vec);
                            loc.getWorld().spawnParticle(VParticle.REDSTONE.get(), loc, 1, new Particle.DustOptions(Color.WHITE, .7f));
                            loc.add(vec.multiply(-1));
                        }

                    for (Entity entity : entities)
                        if (UtilityMethods.canTarget(caster, loc, entity)) {
                            loc.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), loc, 0);
                            loc.getWorld().spawnParticle(VParticle.FIREWORK.get(), loc, 48, 0, 0, 0, .2);
                            loc.getWorld().playSound(loc, Sounds.ENTITY_GENERIC_EXPLODE, 2, 1);
                            skillMeta.getCaster().attack((LivingEntity) entity, skillMeta.getParameter("damage"), DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE);
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(),
                                    (int) (skillMeta.getParameter("duration") * 20), (int) skillMeta.getParameter("amplifier")));
                            cancel();
                            return;
                        }
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
