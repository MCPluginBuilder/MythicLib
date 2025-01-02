package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Wither extends SkillHandler<TargetSkillResult> {
    public Wither() {
        super();

        registerModifiers("duration", "amplifier");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();

        new BukkitRunnable() {
            final Location loc = target.getLocation();
            double y = 0;

            public void run() {
                if (y > 3)
                    cancel();

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
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_WITHER_SHOOT, 2, 2);
        target.addPotionEffect(
                new PotionEffect(PotionEffectType.WITHER, (int) (skillMeta.getParameter("duration") * 20), (int) skillMeta.getParameter("amplifier")));
    }
}
