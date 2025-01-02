package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.ParabolicProjectile;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Telekinesy extends SkillHandler<TargetSkillResult> {
    public Telekinesy() {
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
        new TelekinesyRunnable(skillMeta.getCaster().getData(), result.getTarget(), skillMeta.getParameter("duration"), skillMeta.getParameter("knockback") / 100);
    }

    public static class TelekinesyRunnable extends BukkitRunnable implements Listener {
        private final Entity entity;
        private final MMOPlayerData caster;

        private final long duration;
        private final double d, f;

        private int j;

        public TelekinesyRunnable(MMOPlayerData caster, Entity entity, double duration, double force) {
            this.entity = entity;
            this.caster = caster;

            d = caster.getPlayer().getLocation().distance(entity.getLocation());
            f = force;
            this.duration = (long) (20 * duration);

            runTaskTimer(MythicLib.plugin, 0, 1);
            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
        }

        @EventHandler
        public void a(PlayerInteractEvent event) {
            if (event.getPlayer().equals(caster.getPlayer()) && event.getAction().name().contains("LEFT_CLICK")) {
                entity.setVelocity(caster.getPlayer().getEyeLocation().getDirection().multiply(1.5 * f));
                entity.getWorld().playSound(entity.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 2, 1);
                entity.getWorld().spawnParticle(VParticle.WITCH.get(), entity.getLocation().add(0, entity.getHeight() / 2, 0), 16);
                close();
            }
        }

        @Override
        public void run() {
            if (UtilityMethods.isInvalidated(caster) || entity.isDead() || j++ >= duration) {
                close();
                return;
            }

            if (j % 8 == 0)
                new ParabolicProjectile(caster.getPlayer().getEyeLocation(), entity.getLocation().add(0, entity.getHeight() / 2, 0), VParticle.WITCH.get());

            Location loc = caster.getPlayer().getEyeLocation().add(caster.getPlayer().getEyeLocation().getDirection().multiply(d));
            entity.setVelocity(loc.subtract(entity.getLocation().add(0, entity.getHeight() / 2, 0)).toVector().multiply(2));
            entity.setFallDistance(0);
        }

        private void close() {
            cancel();
            HandlerList.unregisterAll(this);
        }
    }
}
