package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ShulkerMissile extends SkillHandler<VectorSkillResult> {
    public ShulkerMissile() {
        super("SHULKER_MISSILE");

        registerModifiers("damage", "effect-duration", "duration");
    }

    @NotNull
    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        final Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            double n = 0;

            public void run() {
                if (n++ > 3) {
                    cancel();
                    return;
                }

                caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_WITHER_SHOOT, 2, 2);
                ShulkerBullet shulkerBullet = (ShulkerBullet) caster.getWorld().spawnEntity(caster.getLocation().add(0, 1, 0), EntityType.SHULKER_BULLET);
                shulkerBullet.setShooter(caster);
                new ShulkerMissileHandler(skillMeta.getCaster(), shulkerBullet, result.getTarget(), (long) (skillMeta.getParameter("duration") * 20), skillMeta.getParameter("damage"), (int) (20 * skillMeta.getParameter("effect-duration")));
            }
        }.runTaskTimer(MythicLib.plugin, 0, 3);
    }

    public static class ShulkerMissileHandler extends TemporaryListener {
        private final PlayerMetadata caster;
        private final ShulkerBullet bullet;
        private final Vector vel;
        private final long duration;
        private final double damage;
        private final int effectDuration;

        public ShulkerMissileHandler(PlayerMetadata caster, ShulkerBullet bullet, Vector vel, long duration, double damage, int effectDuration) {
            this.caster = caster;
            this.bullet = bullet;
            this.vel = vel;
            this.duration = duration;
            this.damage = damage;
            this.effectDuration = effectDuration;

            registerRunnable(new BukkitRunnable() {
                double ti = 0;

                public void run() {
                    if (bullet.isDead() || ti++ >= duration) close();
                    else bullet.setVelocity(vel);
                }
            }, runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        @Override
        public void whenClosed() {
            if (!bullet.isDead()) bullet.remove();
        }

        @EventHandler
        public void onHit(EntityDamageByEntityEvent event) {
            if (!event.getDamager().equals(bullet)) return;

            if (!UtilityMethods.canTarget(caster.getPlayer(), null, event.getEntity(), InteractionType.OFFENSE_SKILL)) {
                event.setCancelled(true);
                return;
            }

            final LivingEntity entity = (LivingEntity) event.getEntity();
            event.setDamage(damage);

            new BukkitRunnable() {
                final Location loc = entity.getLocation();
                double y = 0;

                public void run() {

                    // Potion effect should apply right after the damage with a 1 tick delay.
                    if (y == 0) {
                        entity.removePotionEffect(PotionEffectType.LEVITATION);
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, effectDuration, 0));
                    }

                    for (int j1 = 0; j1 < 3; j1++) {
                        y += .04;
                        for (int j = 0; j < 2; j++) {
                            double xz = y * Math.PI * 1.3 + (j * Math.PI);
                            loc.getWorld().spawnParticle(VParticle.REDSTONE.get(), loc.clone().add(Math.cos(xz), y, Math.sin(xz)), 1, new Particle.DustOptions(Color.MAROON, 1));
                        }
                    }
                    if (y >= 2) cancel();
                }
            }.runTaskTimer(MythicLib.plugin, 0, 1);
        }
    }
}
