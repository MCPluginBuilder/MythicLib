package io.lumine.mythic.lib.script.targeter.entity;

import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ConeTargeter implements EntityTargeter {
    private final NumericExpression radius, angle;
    private final LocationTargeter sourceLocation, direction;

    public ConeTargeter(ConfigObject config) {
        sourceLocation = config.contains("source") ? config.getLocationTargeter("source") : null;
        direction = config.contains("direction") ? config.getLocationTargeter("direction") : null;

        angle = config.numericExpr("angle");
        radius = config.numericExpr("radius");
    }

    @Override
    public List<Entity> findTargets(SkillMetadata meta) {

        Location loc = sourceLocation == null ? meta.getCaster().getPlayer().getEyeLocation() : sourceLocation.findTargets(meta).get(0);
        Vector dir = direction == null ? loc.getDirection() : direction.findTargets(meta).get(0).toVector();

        double rad = radius.evaluate(meta);
        double angle = Math.toRadians(this.angle.evaluate(meta));

        var list = new ArrayList<Entity>();
        for (Entity nearby : loc.getWorld().getNearbyEntities(loc, rad, rad, rad))
            if (nearby.getLocation().subtract(loc).toVector().angle(dir) < angle && !nearby.equals(meta.getCaster().getPlayer()))
                list.add(nearby);

        return list;
    }
}
