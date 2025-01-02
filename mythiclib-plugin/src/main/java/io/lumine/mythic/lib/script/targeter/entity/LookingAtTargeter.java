package io.lumine.mythic.lib.script.targeter.entity;

import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Targets the entity the player is looking at
 */
public class LookingAtTargeter implements EntityTargeter {
    private final DoubleFormula range, size;
    private final boolean ignorePassable;

    public LookingAtTargeter(ConfigObject config) {
        size = config.getDoubleFormula("size", DoubleFormula.constant(.2f));
        range = config.getDoubleFormula("length", DoubleFormula.constant(50));
        ignorePassable = config.getBoolean("ignore_passable", true);
    }

    @Override
    public List<Entity> findTargets(SkillMetadata meta) {
        final double size = this.size.evaluate(meta);
        final double range = this.range.evaluate(meta);

        Validate.isTrue(size >= 0, "Size must be positive");
        Validate.isTrue(range > 0, "Length must be strictly positive");

        Location source = meta.getCaster().getPlayer().getEyeLocation();
        RayTraceResult result = meta.getSourceLocation().getWorld().rayTrace(source, source.getDirection(), range, FluidCollisionMode.NEVER, ignorePassable, size, entity -> true);
        return result == null || result.getHitEntity() == null ? new ArrayList<>() : Arrays.asList(result.getHitEntity());
    }
}
