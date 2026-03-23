package io.lumine.mythic.lib.script.targeter.location;

import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

/**
 * Provides a location which coordinates are provided as parameters
 */
@Orientable
public class CustomLocationTargeter extends LocationTargeter {
    private final NumericExpression x, y, z;
    private final boolean relative, source;

    public CustomLocationTargeter(ConfigObject config) {
        super(config);

        this.x = config.numericExpr("x");
        this.y = config.numericExpr("y");
        this.z = config.numericExpr("z");

        relative = config.getBoolean("relative", true);
        source = config.getBoolean("source", false);
    }

    @Override
    public List<Location> findTargets(SkillMetadata meta) {
        final var loc = relative ? meta.getSkillLocation(source) : new Location(meta.getSourceLocation().getWorld(), 0, 0, 0);
        loc.add(x.evaluate(meta), y.evaluate(meta), z.evaluate(meta));
        return Collections.singletonList(loc);
    }
}
