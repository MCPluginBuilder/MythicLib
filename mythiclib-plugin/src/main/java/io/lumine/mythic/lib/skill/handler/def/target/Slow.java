package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class Slow extends SkillHandler<TargetSkillResult> {
    public Slow() {
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
            double ti = 0;

            public void run() {
                ti += Math.PI / 10;
                if (ti >= Math.PI * 2)
                    cancel();

                for (double j = 0; j < Math.PI * 2; j += Math.PI)
                    for (double r = 0; r < .7; r += .1)
                        loc.getWorld().spawnParticle(VParticle.REDSTONE.get(),
                                loc.clone().add(Math.cos((ti / 2) + j + (Math.PI * r)) * r * 2, .1, Math.sin((ti / 2) + j + (Math.PI * r)) * r * 2),
                                1, new Particle.DustOptions(Color.WHITE, 1));

            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_LLAMA_ANGRY, 1, 2);
        target.addPotionEffect(
                new PotionEffect(VPotionEffectType.SLOWNESS.get(), (int) (skillMeta.getParameter("duration") * 20), (int) skillMeta.getParameter("amplifier")));
    }
}
