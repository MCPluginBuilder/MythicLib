package io.lumine.mythic.lib.script.mechanic.type;

import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.CasterLocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.DefaultDirectionTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.EntityLocationType;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Used when a mechanic requires a direction as parameter
 * to be cast. Examples:
 * - ray traces
 * - projectiles
 * <p>
 * By default these are always emitted from the caster's eyes
 */
public abstract class DirectionMechanic extends Mechanic {
    private final LocationTargeter sourceLocation, targetLocation;

    public DirectionMechanic(ConfigObject config) {
        sourceLocation = config.contains("source") ? config.getLocationTargeter("source") : new CasterLocationTargeter(EntityLocationType.EYES);
        targetLocation = config.contains("target") ? config.getLocationTargeter("target") : new DefaultDirectionTargeter();
    }

    public LocationTargeter getSource() {
        return sourceLocation;
    }

    public LocationTargeter getTarget() {
        return targetLocation;
    }

    @Override
    public void cast(SkillMetadata meta) {

        // This better not be empty
        final Location source = this.sourceLocation.findTargets(meta).get(0);
        final Vector sourceVector = source.toVector();

        for (Location loc : targetLocation.findTargets(meta)) {
            final Vector dir = loc.toVector().subtract(sourceVector);
            Validate.isTrue(dir.lengthSquared() > 0, "Direction cannot be zero");
            cast(meta, source, dir.normalize());
        }
    }

    /**
     * @param meta      Skill metadata
     * @param source    Source location
     * @param direction Normalized direction vector
     */
    public abstract void cast(SkillMetadata meta, Location source, Vector direction);
}
