package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class Frozen_Aura extends SkillHandler<SimpleSkillResult> {
    public Frozen_Aura() {
        super();

        registerModifiers("duration", "amplifier", "radius");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration") * 20;
        double radiusSquared = Math.pow(skillMeta.getParameter("radius"), 2);
        double amplifier = skillMeta.getParameter("amplifier") - 1;

        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            double j = 0;
            int ti = 0;

            public void run() {
                if (ti++ > duration)
                    cancel();

                j += Math.PI / 60;
                for (double k = 0; k < Math.PI * 2; k += Math.PI / 2)
                    caster.getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(), caster.getLocation().add(Math.cos(k + j) * 2, 1 + Math.sin(k + j * 7) / 3, Math.sin(k + j) * 2), 0);

                if (ti % 2 == 0)
                    caster.getWorld().playSound(caster.getLocation(), Sounds.BLOCK_SNOW_BREAK, 1, 1);

                if (ti % 7 == 0)
                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(caster.getLocation()))
                        if (entity.getLocation().distanceSquared(caster.getLocation()) < radiusSquared && UtilityMethods.canTarget(caster, entity))
                            UtilityMethods.forcePotionEffect((LivingEntity) entity, VPotionEffectType.SLOWNESS.get(), 2, (int) amplifier);
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
