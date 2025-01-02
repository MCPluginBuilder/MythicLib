package io.lumine.mythic.lib.skill.handler.def.location;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Black_Hole extends SkillHandler<LocationSkillResult> {
    public Black_Hole() {
        super();

        registerModifiers("radius", "duration");
    }

    @Override
    public LocationSkillResult getResult(SkillMetadata meta) {
        return new LocationSkillResult(meta);
    }

    @Override
    public void whenCast(LocationSkillResult result, SkillMetadata skillMeta) {
        Location loc = result.getTarget();
        Player caster = skillMeta.getCaster().getPlayer();

        double duration = skillMeta.getParameter("duration") * 20;
        double radius = skillMeta.getParameter("radius");

        loc.getWorld().playSound(loc, Sounds.ENTITY_ENDERMAN_TELEPORT, 3, 1);
        new BukkitRunnable() {
            int ti = 0;
            final double r = 4;

            public void run() {
                if (ti++ > Math.min(300, duration))
                    cancel();

                loc.getWorld().playSound(loc, Sounds.BLOCK_NOTE_BLOCK_HAT, 2, 2);
                loc.getWorld().spawnParticle(VParticle.LARGE_EXPLOSION.get(), loc, 0);
                for (int j = 0; j < 3; j++) {
                    double ran = RANDOM.nextDouble() * Math.PI * 2;
                    double ran_y = RANDOM.nextDouble() * 2 - 1;
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
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
