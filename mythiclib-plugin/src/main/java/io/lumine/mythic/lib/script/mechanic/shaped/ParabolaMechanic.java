package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.SourceLocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.TargetLocationTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Draws a parabola from point A to point B.
 * You can configure the parabola height and speed
 */
public class ParabolaMechanic extends Mechanic {
    private final double height, speed;

    private final LocationTargeter sourceLocation, targetLocation;

    private final Script onStart, onTick, onEnd;

    public ParabolaMechanic(ConfigObject config) {
        sourceLocation = config.contains("source") ? config.getLocationTargeter("source") : new SourceLocationTargeter();
        targetLocation = config.contains("target") ? config.getLocationTargeter("target") : new TargetLocationTargeter();

        config.validateKeys("tick");

        onStart = config.contains("start") ? config.getScript("start") : null;
        onTick = config.getScript("tick");
        onEnd = config.contains("end") ? config.getScript("end") : null;

        height = config.getDouble("height");
        speed = config.getDouble("speed", 1);

        Validate.isTrue(speed > 0, "Speed must be strictly positive");
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

        // Distance between the two points protected onto the XZ plane
        final double xzLength = dir.clone().setY(0).length();

        /*
         * Let y = a.x.(x - b) be the parabola we are looking for
         * There is no 0 constant in that polynomial because to simplify
         * calculations we are looking for y = 0 at x = 0
         *
         * The two conditions are:
         * 1) y = h when x = l / 2
         * 2) y = z when x = l
         *
         * This gives a one-solution (existence is granted by Lagrange
         * polynomial interpolation) which coefficients are:
         * a = (2.z - 4.h) / l²
         * b = l - z / (2.z - 4.h)
         *
         * This does NOT work when z = 2.h because the solution is
         * a first degree polynomial and none exist under the y = a.x.(x - b)
         * factorized form. To make sure that doesn't happen, the
         * height parameter is defined relative to the highest Y
         * coordinate of source and target location. This also
         * looks better in game and makes a little more sense.
         */
        final double height = this.height + Math.max(0, dir.getY());
        final double a = (2 * dir.getY() - 4 * height) / (xzLength * xzLength);
        final double b = xzLength - dir.getY() / (a * xzLength);

        new BukkitRunnable() {
            double x = 0;

            private static final double DT = .05;

            // Max distance between two particles
            private static final double STEP = .3;

            private final Vector axis = dir.clone().setY(0).normalize();

            @Override
            public void run() {

                // Distance traveled along the x axis
                final double dx = speed * DT;
                if (x >= xzLength) {
                    cancel();
                    return;
                }

                // Distance traveled along the parabola
                final double dy = getLength(a, b, x, x + dx);
                x += dx;

                // Amount of particles to display this tick
                final int displayed = Math.max(1, (int) (dy / STEP));
                final double xStep = dx / (double) displayed;

                // Script being cast
                final Script cast = onStart != null && x == dx ? onStart : (onEnd != null && x >= xzLength ? onEnd : onTick);
                for (int i = 0; i < displayed; i++) {

                    // Intermediate x value and corresponding location
                    double x_i = x + i * xStep;
                    Location loc_i = source.clone().add(axis.clone().multiply(x_i)).add(0, y(a, b, x_i), 0);
                    cast.cast(meta.clone(source, loc_i, null, null));
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }

    /**
     * @return y coordinate of point on parabola (x, y)
     */
    private double y(double a, double b, double x) {
        return a * x * (x - b);
    }

    /**
     * @return Length of parabola between two x values
     */
    private double getLength(double a, double b, double x1, double x2) {
        double u1 = a * (2 * x1 - b);
        double u2 = a * (2 * x2 - b);
        return (primitive(u2) - primitive(u1)) / (2 * a);
    }

    /**
     * Implementation of the unique primitive of <code>f(x) = sqrt(1 + x²)</code>
     * with null integration constant
     * <p>
     * Source:
     * https://math.stackexchange.com/questions/2660140/integral-int-sqrt1x2dx
     *
     * @return Value of that primitive at given x
     */
    private double primitive(double x) {
        return .5 * x + .25 * Math.sinh(2 * x);
    }
}
