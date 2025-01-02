package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Control extends SkillHandler<TargetSkillResult> {
    public Control() {
        super();

        registerModifiers("knockback", "duration");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();
        caster.getWorld().playSound(caster.getLocation(), Sounds.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
        result.getTarget().addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), 20 * 2, 0));
        new TelekinesyRunnable(skillMeta.getCaster(), result.getTarget(), skillMeta.getParameter("knockback") / 100, skillMeta.getParameter("duration"));
    }

    public static class TelekinesyRunnable extends BukkitRunnable implements Listener {
        private final LivingEntity entity;
        private final PlayerMetadata caster;

        private final double f, d;

        private int j;

        public TelekinesyRunnable(PlayerMetadata caster, LivingEntity entity, double force, double duration) {
            this.entity = entity;
            this.caster = caster;

            d = duration * 20;
            f = force;

            runTaskTimer(MythicLib.plugin, 0, 1);
            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
        }

        @EventHandler
        public void a(PlayerInteractEvent event) {
            if (event.getPlayer().equals(caster.getPlayer()) && event.getAction().name().contains("LEFT_CLICK")) {
                Vector vec = caster.getPlayer().getEyeLocation().getDirection().multiply(3 * f);
                vec.setY(Math.max(.5, vec.getY() / 2));
                entity.setVelocity(vec);

                // Try not to interfere with other potion effects
                PotionEffect effect = entity.getPotionEffect(VPotionEffectType.SLOWNESS.get());
                if (effect.getDuration() < d && effect.getAmplifier() == 0)
                    entity.removePotionEffect(VPotionEffectType.SLOWNESS.get());

                entity.getWorld().spawnParticle(VParticle.WITCH.get(), entity.getLocation().add(0, entity.getHeight() / 2, 0), 16);
                entity.getWorld().playSound(entity.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 2, 1);
                close();
            }
        }

        @Override
        public void run() {
            if (UtilityMethods.isInvalidated(caster) || entity.isDead() || j++ >= d) {
                close();
                return;
            }

            double a = (double) j / 3;
            entity.getWorld().spawnParticle(VParticle.WITCH.get(), entity.getLocation().add(Math.cos(a), entity.getHeight() / 2, Math.sin(a)), 0);
        }

        private void close() {
            cancel();
            HandlerList.unregisterAll(this);
        }
    }
}
