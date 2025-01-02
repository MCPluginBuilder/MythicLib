package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.CasterLocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.DefaultDirectionTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.EntityLocationType;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Performs what looks like a sword slash in
 * front of the caster/target entity/target location.
 */
public class SlashMechanic extends Mechanic {
    private final double length, angle, distance;
    private final long points, timeInterval, pointsPerTick;

    private final LocationTargeter sourceLocation, targetLocation;

    private final Script onStart, onTick, onEnd;

    public SlashMechanic(ConfigObject config) {
        sourceLocation = config.contains("source") ? config.getLocationTargeter("source") : new CasterLocationTargeter(EntityLocationType.EYES);
        targetLocation = config.contains("target") ? config.getLocationTargeter("target") : new DefaultDirectionTargeter();

        config.validateKeys("tick");

        onStart = config.contains("start") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("start")) : null;
        onTick = MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("tick"));
        onEnd = config.contains("end") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("end")) : null;

        distance = config.getDouble("distance", 3);
        length = config.getDouble("length", 4);
        angle = config.getDouble("angle", -30);

        points = config.getInt("points", 20);
        timeInterval = config.getInt("time_interval", 1);
        pointsPerTick = config.getInt("points_per_tick", 1);

        Validate.isTrue(length > 0, "Length must be strictly positive");
        Validate.isTrue(points > 0, "Points must be strictly positive");
        Validate.isTrue(timeInterval > 0, "Time interval must be strictly positive");
        Validate.isTrue(pointsPerTick > 0, "Points per tick must be strictly positive");
    }

    @Override
    public void cast(SkillMetadata meta) {

        // This better not be empty
        Location source = this.sourceLocation.findTargets(meta).get(0);

        for (Location loc : targetLocation.findTargets(meta))
            cast(meta, source, loc.clone().subtract(source).toVector());
    }

    public void cast(SkillMetadata meta, Location source, Vector dir) {
        Validate.isTrue(dir.lengthSquared() > 0, "Direction cannot be zero");

        final Vector radialAxis = dir.clone().normalize();
        final Vector slashDirection = dir.clone().setY(0).rotateAroundY(-Math.PI / 2).rotateAroundAxis(radialAxis, Math.toRadians(angle)).normalize();

        new BukkitRunnable() {

            // Tick counter
            int counter = 0;

            final Location current = source.clone().add(slashDirection.clone().multiply(-length / 2));
            final Vector incremented = slashDirection.clone().multiply(length / points);

            public void run() {

                for (int i = 0; i < pointsPerTick; i++) {

                    /* Always on start otherwise an error in the
                     * code will make the runnable loop forever.
                     */
                    if (counter++ >= points) {
                        cancel();
                        return;
                    }

                    // Move the current slash location
                    current.add(incremented);

                    // Add some curvature using f(x) = sqrt(1 - x²) to find the real location
                    final double x = Math.abs(counter - points / 2) * 2d / points;
                    final Script cast = onStart != null && counter == 1 ? onStart : (onEnd != null && counter >= points ? onEnd : onTick);
                    Location intermediate = current.clone().add(dir.clone().multiply(distance * Math.sqrt(1 - x * x)));
                    cast.cast(meta.clone(source, intermediate, null, null));
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, timeInterval);
    }
}
