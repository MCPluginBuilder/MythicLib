package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Magical_Path extends SkillHandler<SimpleSkillResult> {
    public Magical_Path() {
        super();

        registerModifiers("duration");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        new MagicalPathEffect(skillMeta.getCaster().getPlayer(), skillMeta.getParameter("duration"));
    }

    public class MagicalPathEffect extends BukkitRunnable implements Listener {
        private final Player player;
        private final long duration;

        /*
         * when true, the next fall damage is negated
         */
        private boolean safe = true;

        private int j = 0;

        public MagicalPathEffect(Player player, double duration) {
            this.player = player;
            this.duration = (long) (duration * 10);

            player.setAllowFlight(true);
            player.setFlying(true);
            player.setVelocity(player.getVelocity().setY(.5));
            player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);

            runTaskTimer(MythicLib.plugin, 0, 2);
            Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
        }

        public void close() {
            player.setAllowFlight(false);
            HandlerList.unregisterAll(this);
            cancel();
        }

        @EventHandler(priority = EventPriority.LOW)
        public void a(EntityDamageEvent event) {
            if (safe && event.getEntity().equals(player) && event.getCause() == DamageCause.FALL) {
                event.setCancelled(true);
                safe = false;

                player.getWorld().spawnParticle(VParticle.EFFECT.get(), player.getLocation(), 8, .35, 0, .35, .08);
                player.getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(), player.getLocation(), 16, .35, 0, .35, .08);
                player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_HURT, 1, 2);
            }
        }

        @EventHandler
        public void b(PlayerQuitEvent event) {
            if (event.getPlayer().equals(player)) close();
        }

        @Override
        public void run() {

            if (j++ > duration) {
                player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.setAllowFlight(false);
                cancel();
                return;
            }

            player.getWorld().spawnParticle(VParticle.EFFECT.get(), player.getLocation(), 8, .5, 0, .5, .1);
            player.getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(), player.getLocation(), 16, .5, 0, .5, .1);
        }
    }
}
