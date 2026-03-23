package io.lumine.mythic.lib.script.targeter.location;

import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;

import java.util.Collections;
import java.util.List;

/**
 * Targets the block the player is looking at. Precisely, it uses a ray trace
 * to find the exact location where his line of sight hits the target block.
 */
public class LookingAtTargeter extends LocationTargeter {
    private final double rayCastLength;
    private final boolean ignorePassable;

    public LookingAtTargeter(ConfigObject config) {
        super(false);

        this.rayCastLength = config.getDouble("length", 50);
        ignorePassable = config.getBoolean("ignore_passable", true);
    }

    @Override
    public List<Location> findTargets(SkillMetadata meta) {
        Location source = meta.getCaster().getPlayer().getEyeLocation();
        RayTraceResult result = meta.getSourceLocation().getWorld().rayTraceBlocks(source, source.getDirection(), rayCastLength, FluidCollisionMode.NEVER, ignorePassable);
        return result == null ? Collections.emptyList() : Collections.singletonList(result.getHitPosition().toLocation(source.getWorld()));
    }
}
