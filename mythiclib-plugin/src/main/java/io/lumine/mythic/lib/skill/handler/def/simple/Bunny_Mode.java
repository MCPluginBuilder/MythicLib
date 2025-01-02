package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Bunny_Mode extends SkillHandler<SimpleSkillResult> {
    public Bunny_Mode() {
        super();

        registerModifiers("duration", "jump-force", "speed");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    private static final long JUMP_COOLDOWN = 300;

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration") * 20;
        double y = skillMeta.getParameter("jump-force");
        double xz = skillMeta.getParameter("speed");

        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            final BunnyModeEffect handler = new BunnyModeEffect(caster);
            int j = 0;

            long lastJump = 0;

            public void run() {
                if (j++ > duration) {
                    handler.close(3 * 20);
                    cancel();
                    return;
                }

                if (caster.getLocation().add(0, -.3, 0).getBlock().getType().isSolid() && System.currentTimeMillis() - lastJump > JUMP_COOLDOWN) {
                    lastJump = System.currentTimeMillis();
                    final Vector dir = UtilityMethods.safeNormalize(caster.getEyeLocation().getDirection().setY(0), new Vector(0, 1, 0));
                    caster.setVelocity(dir.multiply(.8 * xz).setY(0.5 * y));
                    caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDER_DRAGON_FLAP, 2, 1);
                    for (double a = 0; a < Math.PI * 2; a += Math.PI / 12)
                        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation(), 0, Math.cos(a), 0, Math.sin(a), .2);
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }

    public static class BunnyModeEffect extends TemporaryListener {
        private final Player player;

        public BunnyModeEffect(Player player) {
            this.player = player;
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void a(EntityDamageEvent event) {
            if (event.getEntity().equals(player) && event.getCause() == DamageCause.FALL) event.setCancelled(true);
        }
    }
}
