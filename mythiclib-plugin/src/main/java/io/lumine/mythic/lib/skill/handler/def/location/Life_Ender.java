package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
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

@BuiltinSkillHandler(mods = {"damage", "knockback", "radius"})
public class Life_Ender extends SkillHandler<LocationSkillResult> {
    private final List<DamageType> damageTypes;

    public Life_Ender(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.SKILL, DamageType.MAGIC), config.get("damage_types"));
    }

    @Override
    public @NotNull LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double damage = skillMeta.getParameter("damage");
        double knockback = skillMeta.getParameter("knockback");
        double radius = skillMeta.getParameter("radius");

        caster.getWorld().playSound(caster.getLocation(), Sounds.ENTITY_ENDERMAN_TELEPORT, 2, 1);
        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final Location source = loc.clone().add(5 * Math.cos(Math.random() * 2 * Math.PI), 20, 5 * Math.sin(Math.random() * 2 * Math.PI));
            final Vector vec = loc.subtract(source).toVector().multiply((double) 1 / 30);
            int ti = 0;

            public void run() {
                if (ti == 0)
                    loc.setDirection(vec);

                for (int k = 0; k < 2; k++) {
                    ti++;
                    source.add(vec);
                    for (double i = 0; i < Math.PI * 2; i += Math.PI / 6) {
                        Vector vec = UtilityMethods.rotate(new Vector(Math.cos(i), Math.sin(i), 0), loc.getDirection());
                        source.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), source, 0, vec.getX(), vec.getY(), vec.getZ(), .1);
                    }
                }

                if (ti >= 30) {
                    source.getWorld().playSound(source, Sounds.ENTITY_GENERIC_EXPLODE, 3, 1);
                    source.getWorld().spawnParticle(Particle.FLAME, source, 64, 0, 0, 0, .25);
                    source.getWorld().spawnParticle(Particle.LAVA, source, 32);
                    for (double j = 0; j < Math.PI * 2; j += Math.PI / 24)
                        source.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), source, 0, Math.cos(j), 0, Math.sin(j), .5);

                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(source))
                        if (entity.getLocation().distanceSquared(source) < radius * radius && UtilityMethods.canTarget(caster, entity)) {
                            skillMeta.getCaster().attack((LivingEntity) entity, damage, damageTypes);
                            entity.setVelocity(entity.getLocation().subtract(source).toVector().setY(.75).normalize().multiply(knockback));
                        }

                    handler.close();
                }
            }
        });
    }
}
