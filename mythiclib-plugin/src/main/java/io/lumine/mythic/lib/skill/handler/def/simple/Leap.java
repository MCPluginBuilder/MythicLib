package io.lumine.mythic.lib.skill.handler.def.simple;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"force"})
public class Leap extends SkillHandler<SimpleSkillResult> {
    public Leap(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull SimpleSkillResult getResult(SkillMetadata meta) {
        return new SimpleSkillResult(meta.getCaster().getPlayer().isOnGround());
    }

    @Override
    public void whenCast(SimpleSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDER_DRAGON_FLAP, 1, 0);
        caster.getWorld().spawnParticle(VParticle.EXPLOSION.get(), caster.getLocation(), 16, 0, 0, 0.1);
        Vector vec = caster.getEyeLocation().getDirection().multiply(2 * skillMeta.getParameter("force"));
        vec.setY(vec.getY() / 2);
        caster.setVelocity(vec);

        // Temporary handler bc particle effect needs to stop
        // if the player leaves
        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            double ti = 0;

            public void run() {
                if (++ti > 20) handler.close();
                else caster.getWorld().spawnParticle(Particle.CLOUD, caster.getLocation().add(0, 1, 0), 0);
            }
        });
    }
}
