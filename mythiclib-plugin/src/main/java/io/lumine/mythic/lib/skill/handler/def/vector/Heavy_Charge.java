package io.lumine.mythic.lib.skill.handler.def.vector;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "knockback"})
public class Heavy_Charge extends SkillHandler<VectorSkillResult> {
    private final List<DamageType> damageTypes;

    public Heavy_Charge(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.PHYSICAL), config.get("damage_types"));
    }

    @Override
    public @NotNull VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        double knockback = skillMeta.getParameter("knockback");

        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final Vector vec = result.getTarget().setY(-1);
            double ti = 0;

            public void run() {
                if (ti++ > 20) {
                    handler.close();
                    return;
                }

                if (ti < 9) {
                    caster.setVelocity(vec);
                    caster.getWorld().spawnParticle(VParticle.EXPLOSION.get(), caster.getLocation().add(0, 1, 0), 3, .13, .13, .13, 0);
                }

                for (Entity target : caster.getNearbyEntities(1, 1, 1))
                    if (UtilityMethods.canTarget(caster, target)) {
                        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 1);
                        caster.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), target.getLocation().add(0, 1, 0), 0);
                        target.setVelocity(caster.getVelocity().setY(0.3).multiply(1.7 * knockback));
                        caster.setVelocity(caster.getVelocity().setX(0).setY(0).setZ(0));
                        skillMeta.getCaster().attack((LivingEntity) target, skillMeta.getParameter("damage"), damageTypes);
                        handler.close();
                        break;
                    }
            }
        });
    }
}
