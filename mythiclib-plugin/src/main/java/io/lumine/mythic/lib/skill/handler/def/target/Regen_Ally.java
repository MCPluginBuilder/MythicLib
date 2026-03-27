package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.player.resource.Resources;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"heal", "duration"})
public class Regen_Ally extends SkillHandler<TargetSkillResult> {
    public Regen_Ally(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta, InteractionType.SUPPORT_SKILL);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();

        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final double duration = Math.min(skillMeta.getParameter("duration"), 60) * 20;
            final double hps = skillMeta.getParameter("heal") / duration * 4;
            double ti = 0;
            double a = 0;

            public void run() {
                if (ti++ > duration || target.isDead()) {
                    handler.close();
                    return;
                }

                a += Math.PI / 16;
                target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(1.3 * Math.cos(a), .3, 1.3 * Math.sin(a)), 0);

                if (ti % 4 == 0) Resources.heal(target, hps);
            }
        });
    }
}
