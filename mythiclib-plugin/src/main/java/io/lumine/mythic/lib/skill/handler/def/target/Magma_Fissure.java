package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"ignite", "damage"})
public class Magma_Fissure extends SkillHandler<TargetSkillResult> {
    private final List<DamageType> damageTypes;

    public Magma_Fissure(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC), config.get("damage_types"));
    }

    @Override
    public @NotNull TargetSkillResult getResult(SkillMetadata meta) {
        return new TargetSkillResult(meta);
    }

    @Override
    public void whenCast(TargetSkillResult result, SkillMetadata skillMeta) {
        LivingEntity target = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final Location loc = caster.getLocation().add(0, .2, 0);
            int j = 0;

            public void run() {
                if (target.isDead() || !target.getWorld().equals(loc.getWorld()) || ++j > 200) {
                    handler.close();
                    return;
                }

                Vector vec = target.getLocation().add(0, .2, 0).subtract(loc).toVector().normalize().multiply(.6);
                loc.add(vec);

                loc.getWorld().spawnParticle(Particle.LAVA, loc, 2, .2, 0, .2, 0);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 2, .2, 0, .2, 0);
                loc.getWorld().spawnParticle(VParticle.SMOKE.get(), loc, 2, .2, 0, .2, 0);
                loc.getWorld().playSound(loc, Sounds.BLOCK_NOTE_BLOCK_HAT, 1, 1);

                if (target.getLocation().distanceSquared(loc) < 1) {
                    loc.getWorld().playSound(loc, Sounds.ENTITY_BLAZE_HURT, 2, 1);
                    target.setFireTicks((int) (target.getFireTicks() + skillMeta.getParameter("ignite") * 20));
                    skillMeta.getCaster().attack(target, skillMeta.getParameter("damage"), damageTypes);
                    handler.close();
                }
            }
        });
    }
}