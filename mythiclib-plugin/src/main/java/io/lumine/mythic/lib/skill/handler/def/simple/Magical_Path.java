package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class Magical_Path extends SkillHandler<SimpleSkillResult> {
    public Magical_Path() {
        super();

        registerModifiers("duration");
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        new Handler(skillMeta.getCaster().getData(), skillMeta.getParameter("duration"));
    }

    static class Handler extends TemporaryHandler {
        private final Player player;
        private final long duration;

        /*
         * when true, the next fall damage is negated
         */
        private boolean safe = true;

        private int j = 0;

        public Handler(MMOPlayerData playerData, double duration) {
            super(playerData);

            this.player = playerData.getPlayer();
            this.duration = (long) (duration * 10);

            player.setAllowFlight(true);
            player.setFlying(true);
            player.setVelocity(player.getVelocity().setY(.5));
            player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);

            runTask(r -> r.runTaskTimer(MythicLib.plugin, 0, 2));
        }

        @Override
        protected BukkitRunnable newTask() {
            return new BukkitRunnable() {

                @Override
                public void run() {
                    if (j++ > duration) {
                        player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                        player.setAllowFlight(false);
                        Handler.this.close();
                        return;
                    }

                    player.getWorld().spawnParticle(VParticle.EFFECT.get(), player.getLocation(), 8, .5, 0, .5, .1);
                    player.getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(), player.getLocation(), 16, .5, 0, .5, .1);
                }
            };
        }

        @Override
        protected void onClose() {
            player.setAllowFlight(false);
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
    }
}
