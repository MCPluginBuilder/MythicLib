package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * @deprecated Not used yet
 */
@Deprecated
public class SkillOrientation {
    private final Vector pivot;
    private final Vector axis;

    /**
     * Can be used anywhere in location targeters to rotate the target location
     * around some point using a specific axis. The pivot point and skill axis
     * are all provided by skill mechanics which contain some sort of orientation:
     * projectiles, ray traces, slashes...
     *
     * @param pivot Point around which the location will be rotating
     * @param axis  Axis of propagation
     */
    public SkillOrientation(Location pivot, Vector axis) {
        this.pivot = pivot.toVector();
        this.axis = axis;

        Validate.isTrue(axis.lengthSquared() > 0, "Axis cannot be zero");
    }

    /**
     * Applies the right rotation to a target location.
     *
     * @param loc Target location
     * @return Rotated target location
     */
    public Location getRotated(Location loc) {
        return UtilityMethods.rotate(loc.toVector().subtract(pivot), axis).toLocation(loc.getWorld());
    }
}
