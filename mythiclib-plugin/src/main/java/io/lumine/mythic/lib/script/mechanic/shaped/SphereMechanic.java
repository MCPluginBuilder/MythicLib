package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.type.LocationMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;
import org.bukkit.util.Vector;


/**
 * Draws a sphere around target location.
 * <p>
 * Source: Markus Deserno: How to generate equidistributed points
 * on the surface of a sphere. Max-Planck-Institut fur¨
 * Polymerforschung, Ackermannweg 10, 55128 Mainz, Germany
 * <p>
 * The number of points used is not exact but close to the
 * amount of points specified by the user.
 */
public class SphereMechanic extends LocationMechanic {
    private final NumericExpression radius, points;

    private final Script onTick;

    public SphereMechanic(ConfigObject config) {
        super(config);

        onTick = config.getScript("tick");
        radius = config.numericExpr(NumericExpression.of(2), "radius", "rad", "r");
        points = config.numericExpr("points");
    }

    @Override
    public void cast(SkillMetadata meta, Location loc) {
        final double r = radius.evaluate(meta);
        final double N = this.points.evaluate(meta);

        final double a = 4 * Math.PI * r * r / N;
        final double d = Math.sqrt(a);
        final double M_nu = Math.round(Math.PI / d);
        final double d_nu = Math.PI / M_nu;
        final double d_phi = a / d_nu;
        for (int m = 0; m < M_nu; m++) {
            final double nu = Math.PI * (m + 0.5) / M_nu;
            final double M_phi = Math.round(2 * Math.PI * Math.sin(nu) / d_phi);

            for (int n = 0; n < M_phi; n++) {
                final double phi = 2 * Math.PI * n / M_phi;

                // Generate position and cast
                final Location interm = loc.clone().add(toVector(nu, phi).multiply(r));
                onTick.cast(meta.clone(interm));
            }
        }
    }

    private Vector toVector(double nu, double phi) {
        return new Vector(Math.sin(nu) * Math.cos(phi), Math.sin(nu) * Math.sin(phi), Math.cos(nu));
    }
}
