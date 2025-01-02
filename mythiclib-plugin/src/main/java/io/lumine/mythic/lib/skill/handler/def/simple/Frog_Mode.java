package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Frog_Mode extends SkillHandler<SimpleSkillResult> {
    public Frog_Mode() {
        super("FROG_MODE");

        registerModifiers("duration", "jump-force", "speed");
    }

    @Override
    public SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult();
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration") * 20;
        double y = skillMeta.getParameter("jump-force");
        double xz = skillMeta.getParameter("speed");

        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            int j = 0;

            public void run() {
                if (j++ > duration)
                    cancel();

                if (caster.getLocation().getBlock().getType() == Material.WATER) {
                    caster.setVelocity(caster.getEyeLocation().getDirection().setY(0).normalize().multiply(.8 * xz).setY(0.5 / xz * y));
                    caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDER_DRAGON_FLAP, 2, 1);
                    for (double a = 0; a < Math.PI * 2; a += Math.PI / 12)
                        caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation(), 0, Math.cos(a), 0, Math.sin(a), .2);
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
