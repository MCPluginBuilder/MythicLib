package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"duration", "jump-force", "speed"})
public class Bunny_Mode extends SkillHandler<SimpleSkillResult> {
    public Bunny_Mode(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    private static final long JUMP_COOLDOWN = 300;

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        new Handler(skillMeta);
    }

    static class Handler extends TemporaryHandler {
        final Player caster;
        final double duration, jumpStrength, radialVelocity;

        public Handler(SkillMetadata skillMeta) {
            super(skillMeta.getCaster().getData());

            this.caster = skillMeta.getCaster().getPlayer();

            this.duration = skillMeta.getParameter("duration") * 20;
            this.jumpStrength = skillMeta.getParameter("jump-force");
            this.radialVelocity = skillMeta.getParameter("speed");

            runTask(runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, 1));
        }

        @Override
        protected BukkitRunnable newTask() {
            return new BukkitRunnable() {
                int j = 0;
                long lastJump = 0;

                public void run() {
                    if (j++ > duration) {
                        Handler.this.closeAfter(3 * 20);
                        return;
                    }

                    if (caster.getLocation().add(0, -.3, 0).getBlock().getType().isSolid() && System.currentTimeMillis() - lastJump > JUMP_COOLDOWN) {
                        lastJump = System.currentTimeMillis();
                        final Vector dir = UtilityMethods.safeNormalize(caster.getEyeLocation().getDirection().setY(0), new Vector(0, 1, 0));
                        caster.setVelocity(dir.multiply(.8 * radialVelocity).setY(0.5 * jumpStrength));
                        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDER_DRAGON_FLAP, 2, 1);
                        for (double a = 0; a < Math.PI * 2; a += Math.PI / 12)
                            caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation(), 0, Math.cos(a), 0, Math.sin(a), .2);
                    }
                }
            };
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void a(EntityDamageEvent event) {
            if (event.getEntity().equals(caster) && event.getCause() == DamageCause.FALL) event.setCancelled(true);
        }
    }
}
