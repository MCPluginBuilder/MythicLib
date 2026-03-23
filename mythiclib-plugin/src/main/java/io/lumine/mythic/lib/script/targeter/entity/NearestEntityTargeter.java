package io.lumine.mythic.lib.script.targeter.entity;

import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.List;

public class NearestEntityTargeter implements EntityTargeter {
    private final NumericExpression radius;
    private final boolean source;

    public NearestEntityTargeter(ConfigObject config) {
        source = config.getBoolean("source", false);
        radius = config.numericExpr("radius");
    }

    @Override
    public List<Entity> findTargets(SkillMetadata meta) {
        Location loc = meta.getSkillLocation(source);
        double rad = radius.evaluate(meta);

        Entity nearest = null;
        var distSquared = Double.MAX_VALUE;

        for (Entity entity : loc.getWorld().getNearbyEntities(loc, rad, rad, rad)) {
            var checked = entity.getLocation().distanceSquared(loc);
            if (checked < distSquared) {
                nearest = entity;
                distSquared = checked;
            }
        }

        if (nearest != null) return Collections.singletonList(nearest);
        return Collections.emptyList();
    }
}
