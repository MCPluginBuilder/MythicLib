package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.VPotionEffectType;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class Death_Mark extends SkillHandler<TargetSkillResult> {
    public Death_Mark() {
        super();

        registerModifiers("damage", "duration", "amplifier");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();

        double duration = skillMeta.getParameter("duration") * 20;
        double dps = skillMeta.getParameter("damage") / duration * 20;

        new BukkitRunnable() {
            double ti = 0;

            public void run() {
                ti++;
                if (ti > duration || target.isDead()) {
                    cancel();
                    return;
                }

                target.getWorld().spawnParticle(VParticle.INSTANT_EFFECT.get(), target.getLocation(), 4, .2, 0, .2, 0);

                if (ti % 20 == 0)
                    skillMeta.getCaster().attack(target, dps, false, DamageType.SKILL, DamageType.MAGIC);
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_BLAZE_HURT, 1, 2);
        target.removePotionEffect(VPotionEffectType.SLOWNESS.get());
        target.addPotionEffect(new PotionEffect(VPotionEffectType.SLOWNESS.get(), (int) duration, (int) skillMeta.getParameter("amplifier")));
    }
}
