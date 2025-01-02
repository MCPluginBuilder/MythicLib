package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Shock extends SkillHandler<TargetSkillResult> {
    public Shock() {
        super();

        registerModifiers("duration");
    }

    @Override
    public TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double duration = skillMeta.getParameter("duration");

        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_ZOMBIE_PIGMAN_ANGRY, 1, 2);
        new BukkitRunnable() {
            final Location loc = target.getLocation();
            final double rads = Math.toRadians(caster.getEyeLocation().getYaw() - 90);
            double ti = rads;

            public void run() {
                for (int j = 0; j < 3; j++) {
                    ti += Math.PI / 15;
                    target.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), loc.clone().add(Math.cos(ti), 1, Math.sin(ti)), 0);
                }
                if (ti >= Math.PI * 2 + rads)
                    cancel();
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);

        new BukkitRunnable() {
            int ti;

            public void run() {
                if (ti++ > (duration > 300 ? 300 : duration * 10) || target.isDead())
                    cancel();
                else
                    target.playEffect(EntityEffect.HURT);
            }
        }.runTaskTimer(MythicLib.plugin, 0, 2);
    }
}
