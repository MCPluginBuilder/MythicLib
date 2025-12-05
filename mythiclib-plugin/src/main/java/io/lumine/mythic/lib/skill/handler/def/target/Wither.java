package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"duration", "amplifier"})
public class Wither extends SkillHandler<TargetSkillResult> {
    public Wither(ConfigurationSection config) {
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
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_WITHER_SHOOT, 2, 2);
        target.addPotionEffect(
                new PotionEffect(PotionEffectType.WITHER, (int) (skillMeta.getParameter("duration") * 20), (int) skillMeta.getParameter("amplifier")));
    }

    private void playParticleEffect(Location loc) {
        new BukkitRunnable() {
            double y = 0;

            public void run() {
                if (y > 3) cancel();

                for (int j1 = 0; j1 < 3; j1++) {
                    y += .07;
                    for (int j = 0; j < 3; j++) {
                        double a = y * Math.PI + (j * Math.PI * 2 / 3);
                        double x = Math.cos(a) * (3 - y) / 2.5;
                        double z = Math.sin(a) * (3 - y) / 2.5;
                        loc.getWorld().spawnParticle(VParticle.REDSTONE.get(), loc.clone().add(x, y, z), 1, new Particle.DustOptions(Color.BLACK, 1));
                    }
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
