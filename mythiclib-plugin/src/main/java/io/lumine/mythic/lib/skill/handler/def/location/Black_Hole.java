package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.BuiltinSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.util.TemporaryHandler;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VParticle;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@BuiltinSkillHandler(mods = {"radius", "duration"})
public class Black_Hole extends SkillHandler<LocationSkillResult> {
    public Black_Hole(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double duration = skillMeta.getParameter("duration") * 20;
        double radius = skillMeta.getParameter("radius");

        loc.getWorld().playSound(loc, Sounds.ENTITY_ENDERMAN_TELEPORT, 3, 1);
        TemporaryHandler.timerTask(skillMeta.getCaster().getData(), 1, handler -> new BukkitRunnable() {
            int ti = 0;
            final double r = 4;

            public void run() {
                if (ti++ > Math.min(300, duration)) {
                    handler.close();
                    return;
                }

                loc.getWorld().playSound(loc, Sounds.BLOCK_NOTE_BLOCK_HAT, 2, 2);
                loc.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), loc, 0);
                for (int j = 0; j < 3; j++) {
                    double ran = Math.random() * Math.PI * 2;
                    double ran_y = Math.random() * 2 - 1;
                    double x = Math.cos(ran) * Math.sin(ran_y * Math.PI * 2);
                    double z = Math.sin(ran) * Math.sin(ran_y * Math.PI * 2);
                    Location loc1 = loc.clone().add(x * r, ran_y * r, z * r);
                    Vector v = loc.toVector().subtract(loc1.toVector());
                    loc1.getWorld().spawnParticle(VParticle.LARGE_SMOKE.get(), loc1, 0, v.getX(), v.getY(), v.getZ(), .1);
                }

                for (Entity entity : UtilityMethods.getNearbyChunkEntities(loc))
                    if (entity.getLocation().distanceSquared(loc) < Math.pow(radius, 2) && UtilityMethods.canTarget(caster, entity))
                        entity.setVelocity(UtilityMethods.safeNormalize(loc.clone().subtract(entity.getLocation()).toVector()).multiply(.5));
            }
        });
    }
}
