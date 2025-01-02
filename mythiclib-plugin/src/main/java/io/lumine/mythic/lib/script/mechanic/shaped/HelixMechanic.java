package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.ConstantLocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.DefaultLocationTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Draws a helix of particles around the target
 */
public class HelixMechanic extends Mechanic {
    private final DoubleFormula radius;
    private final double yawSpread, height;
    private final long points, timeInterval, pointsPerTick, helixes;
    private final LocationTargeter direction, targetLocation;

    private final Script onStart, onTick, onEnd;

    public HelixMechanic(ConfigObject config) {
        targetLocation = config.contains("source") ? config.getLocationTargeter("source") : new DefaultLocationTargeter();
        direction = config.contains("direction") ? config.getLocationTargeter("direction") : new ConstantLocationTargeter(1, 0, 0);

        config.validateKeys("tick");

        onStart = config.contains("start") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("start")) : null;
        onTick = MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("tick"));
        onEnd = config.contains("end") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("end")) : null;

        yawSpread = config.getDouble("yaw", 360);
        height = config.getDouble("height", 3);
        radius = config.contains("radius") ? new DoubleFormula(config.getString("radius")) : new DoubleFormula(2);

        points = config.getInt("points", 40);
        timeInterval = config.getInt("time_interval", 1);
        pointsPerTick = config.getInt("points_per_tick", 3);
        helixes = config.getInt("helixes", 3);

        Validate.isTrue(yawSpread > 0, "Yaw spread must be strictly positive");
        Validate.isTrue(height > 0, "Height must be strictly positive");
        Validate.isTrue(points > 0, "Points must be strictly positive");
        Validate.isTrue(timeInterval > 0, "Time interval must be strictly positive");
        Validate.isTrue(pointsPerTick > 0, "Points per tick must be strictly positive");
    }

    @Override
    public void cast(SkillMetadata meta) {

        // This better not be empty
        Vector dir = direction.findTargets(meta).get(0).toVector();

        for (Location loc : targetLocation.findTargets(meta))
            cast(meta, loc, dir);
    }

    public void cast(SkillMetadata meta, Location source, Vector dir) {
        Validate.isTrue(dir.lengthSquared() > 0, "Direction cannot be zero");

        final double yaw_i = UtilityMethods.getYawPitch(dir)[0] - yawSpread / 2;
        new BukkitRunnable() {

            // Tick counter
            int counter = 0;

            public void run() {
                for (int i = 0; i < pointsPerTick; i++) {

                    /* Always on start otherwise an error in the
                     * code will makethe runnable loop forever.
                     */
                    if (counter++ >= points) {
                        cancel();
                        return;
                    }

                    final double yaw = Math.toRadians(yaw_i + ((double) counter / points) * yawSpread);
                    final double y = ((double) counter / points) * height, r = radius.evaluate(meta);
                    final Script cast = onEnd != null && counter >= points ? onEnd : (onStart != null && counter == 1 ? onStart : onTick);

                    for (int j = 0; j < helixes; j++) {
                        final double angle = yaw + Math.PI * 2 / helixes * j;
                        Location loc = source.clone().add(r * Math.cos(angle), y, r * Math.sin(angle));
                        cast.cast(meta.clone(source, loc, null, null));
                    }
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, timeInterval);
    }
}
