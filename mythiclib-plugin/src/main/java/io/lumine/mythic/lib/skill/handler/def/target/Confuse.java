package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler
public class Confuse extends SkillHandler<TargetSkillResult> {
    public Confuse(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        playParticleEffect(target.getLocation(), Math.toRadians(caster.getEyeLocation().getYaw() - 90));
        target.getWorld().playSound(target.getLocation(), Sounds.ENTITY_SHEEP_DEATH, 1, 2);

        Location loc = target.getLocation().clone();
        loc.setYaw(target.getLocation().getYaw() - 180);
        target.teleport(loc);
    }

    private void playParticleEffect(Location loc, double startAngle) {
        new BukkitRunnable() {
            double ti = startAngle;

            public void run() {
                for (int j1 = 0; j1 < 3; j1++) {
                    ti += Math.PI / 15;
                    Location loc1 = loc.clone().add(Math.cos(ti), 1, Math.sin(ti));
                    loc.getWorld().spawnParticle(VParticle.WITCH.get(), loc1, 0);
                }
                if (ti >= Math.PI * 2 + startAngle) cancel();
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
