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
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BuiltinSkillHandler(mods = {"damage", "duration"})
public class Contamination extends SkillHandler<LocationSkillResult> {
    private final List<DamageType> damageTypes;

    public Contamination(ConfigurationSection config) {
        super(config);

        damageTypes = DamageType.listFromConfig(List.of(DamageType.MAGIC, DamageType.SKILL), config.get("damage_types"));
    }

    @Override
    public @NotNull LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double duration = Math.min(30, skillMeta.getParameter("duration")) * 20;

        loc.add(0, .1, 0);
        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            final double dps = skillMeta.getParameter("damage") / 2;
            double ti = 0;
            int j = 0;

            public void run() {
                if (++j >= duration) {
                    handler.close();
                    return;
                }

                loc.getWorld().spawnParticle(VParticle.REDSTONE.get(), loc.clone().add(Math.cos(ti / 3) * 5, 0, Math.sin(ti / 3) * 5), 1,
                        new Particle.DustOptions(Color.PURPLE, 1));
                for (int j = 0; j < 3; j++) {
                    ti += Math.PI / 32;
                    double r = Math.sin(ti / 2) * 4;
                    for (double k = 0; k < Math.PI * 2; k += Math.PI * 2 / 3)
                        loc.getWorld().spawnParticle(VParticle.WITCH.get(), loc.clone().add(r * Math.cos(k + ti / 4), 0, r * Math.sin(k + ti / 4)), 0);
                }

                if (j % 10 == 0) {
                    loc.getWorld().playSound(loc, Sounds.ENTITY_ENDERMAN_HURT, 2, 1);
                    for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                        if (UtilityMethods.canTarget(caster, entity) && entity.getLocation().distanceSquared(loc) <= 25)
                            skillMeta.getCaster().attack((LivingEntity) entity, dps, false, damageTypes);
                }
            }
        });
    }
}
