package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"duration"})
public class Magical_Path extends SkillHandler<SimpleSkillResult> {
    public Magical_Path(ConfigurationSection config) {
        super(config);
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

        private final boolean lastAllowFlight, lastFlying;

        private int timer = 0;

        public Handler(MMOPlayerData playerData, double duration) {
            super(playerData);

            this.player = playerData.getPlayer();
            this.duration = (long) (duration * 10);
            this.lastAllowFlight = player.getAllowFlight();
            this.lastFlying = player.isFlying();

            player.setVelocity(player.getVelocity().setY(.5));
            player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);

            runTask(r -> r.runTaskTimer(MythicLib.plugin, 0, 2));
        }

        private static final int SMALL_DELAY = 2;

        @Override
        protected BukkitRunnable newTask() {
            return new BukkitRunnable() {

                @Override
                public void run() {

                    if (timer++ > duration) {
                        player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                        player.setAllowFlight(false);
                        Handler.this.close();
                        return;
                    }

                    // Wait a few ticks before enabling flight to avoid it
                    // being canceled by the block directly below the player
                    if (timer == SMALL_DELAY) {
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    }

                    VParticle.EFFECT.spawnSafeSpell(player.getLocation(), 8, .5, 0, .5, .1);
                    VParticle.INSTANT_EFFECT.spawnSafeSpell(player.getLocation(), 16, .5, 0, .5, .1);
                }
            };
        }

        @Override
        protected void onClose() {
            player.setAllowFlight(lastAllowFlight);
            player.setFlying(lastFlying);
        }

        @EventHandler(priority = EventPriority.LOW)
        public void a(EntityDamageEvent event) {
            if (safe && event.getEntity().equals(player) && event.getCause() == DamageCause.FALL) {
                event.setCancelled(true);
                safe = false;

                VParticle.EFFECT.spawnSafeSpell(player.getLocation(), 8, .35, 0, .35, .08);
                VParticle.INSTANT_EFFECT.spawnSafeSpell(player.getLocation(), 16, .35, 0, .35, .08);
                player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ENDERMAN_HURT, 1, 2);
            }
        }

        @EventHandler
        public void b(PlayerQuitEvent event) {
            if (event.getPlayer().equals(player)) close();
        }
    }
}
