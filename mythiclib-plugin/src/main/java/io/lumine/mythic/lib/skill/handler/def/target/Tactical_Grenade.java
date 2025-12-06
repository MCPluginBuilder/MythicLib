package io.lumine.mythic.lib.skill.handler.def.target;

import io.lumine.mythic.lib.UtilityMethods;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@BuiltinSkillHandler(mods = {"knock-up", "damage", "radius"})
public class Tactical_Grenade extends SkillHandler<TargetSkillResult> {
    private final List<DamageType> damageTypes;

    public Tactical_Grenade(ConfigurationSection config) {
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

        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 12, handler -> new BukkitRunnable() {
            final Location loc = caster.getLocation().add(0, .1, 0);
            final double radius = skillMeta.getParameter("radius");
            final double knockup = .7 * skillMeta.getParameter("knock-up");
            final List<Integer> hit = new ArrayList<>();
            int j = 0;

            public void run() {
                if (target.isDead() || !target.getWorld().equals(loc.getWorld()) || j++ > 200) {
                    handler.close();
                    return;
                }

                Vector vec = target.getLocation().add(0, .1, 0).subtract(loc).toVector();
                vec = vec.length() < 3 ? vec : vec.normalize().multiply(3);
                loc.add(vec);

                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 32, 1, 0, 1, 0);
                loc.getWorld().spawnParticle(VParticle.EXPLOSION.get(), loc, 16, 1, 0, 1, .05);
                loc.getWorld().playSound(loc, Sounds.BLOCK_ANVIL_LAND, 2, 0);
                loc.getWorld().playSound(loc, Sounds.ENTITY_GENERIC_EXPLODE, 2, 1);

                for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                    if (!hit.contains(entity.getEntityId()) && UtilityMethods.canTarget(caster, entity) && entity.getLocation().distanceSquared(loc) < radius * radius) {

                        /*
                         * Stop the runnable as soon as the grenade hits the
                         * initial target, otherwise save it so that it is
                         * not damaged twice by the same skill
                         */
                        if (entity.equals(target)) handler.close();
                        else hit.add(entity.getEntityId());

                        skillMeta.getCaster().attack((LivingEntity) entity, skillMeta.getParameter("damage"), damageTypes);
                        entity.setVelocity(entity.getVelocity().add(offsetVector(knockup)));
                    }
            }
        });
    }

    private Vector offsetVector(double y) {
        return new Vector(2 * (Math.random() - .5), y, 2 * (Math.random() - .5));
    }
}