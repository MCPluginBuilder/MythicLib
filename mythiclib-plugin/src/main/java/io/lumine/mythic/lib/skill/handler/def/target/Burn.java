package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"duration"})
public class Burn extends SkillHandler<TargetSkillResult> {
    public Burn(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();

        playParticleEffect(target.getLocation());
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_BLAZE_HURT, 1, 2);

        /*
         * Entity#getFireTicks() does NOT always return a positive
         * value. For players it returns -20 which reduce the apparent
         * skill duration by one second, hence Math#max(...)
         */
        target.setFireTicks((int) (Math.max(0, target.getFireTicks()) + skillMeta.getParameter("duration") * 20));
    }

    private void playParticleEffect(Location loc) {
        new BukkitRunnable() {
            double y = 0;

            public void run() {
                for (int j1 = 0; j1 < 3; j1++) {
                    y += .04;
                    for (int j = 0; j < 2; j++) {
                        double xz = y * Math.PI * 1.3 + (j * Math.PI);
                        Location loc1 = loc.clone().add(Math.cos(xz), y, Math.sin(xz));
                        loc.getWorld().spawnParticle(Particle.FLAME, loc1, 0);
                    }
                }
                if (y >= 1.7) cancel();
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
