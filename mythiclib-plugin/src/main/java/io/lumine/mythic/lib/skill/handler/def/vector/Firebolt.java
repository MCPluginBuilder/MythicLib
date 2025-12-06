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
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "ignite"})
public class Firebolt extends SkillHandler<VectorSkillResult> {
    private final List<DamageType> damageTypes;

    public Firebolt(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), config.get("damage_types"));
    }

    @Override
    public @NotNull VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        Player caster = skillMeta.getCaster().getPlayer();

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final Vector vec = result.getTarget().multiply(.8);
            final Location loc = caster.getEyeLocation();
            int ti = 0;

            public void run() {
                if (++ti > 20) {
                    handler.close();
                    return;
                }

                List<Entity> entities = UtilityMethods.getNearbyChunkEntities(loc);
                loc.getWorld().playSound(loc, Sounds.BLOCK_FIRE_AMBIENT, 2, 1);
                for (int j = 0; j < 2; j++) {
                    loc.add(vec);
                    if (loc.getBlock().getType().isSolid()) {
                        handler.close();
                        return;
                    }

                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 5, .12, .12, .12, 0);
                    if (Math.random() < .3)
                        loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);
                    for (Entity target : entities)
                        if (UtilityMethods.canTarget(caster, loc, target)) {
                            loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .1);
                            loc.getWorld().spawnParticle(Particle.LAVA, loc, 8, 0, 0, 0, 0);
                            loc.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), loc, 0);
                            loc.getWorld().playSound(loc, Sounds.ENTITY_GENERIC_EXPLODE, 3, 1);
                            skillMeta.getCaster().attack((LivingEntity) target, skillMeta.getParameter("damage"), damageTypes);
                            target.setFireTicks((int) skillMeta.getParameter("ignite") * 20);
                            handler.close();
                            return;
                        }
                }
            }
        });
    }
}
