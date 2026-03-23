package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.DirectionMechanic;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

@MechanicMetadata
public class RayTraceMechanic extends DirectionMechanic {
    private final NumericExpression range, size, step;
    private final Script onHitBlock, onHitEntity, onTick;
    private final boolean ignorePassable, offense, neutral;
    private final RayTraceType rayTraceType;

    public static final NumericExpression
            DEFAULT_RANGE = NumericExpression.of(50),
            DEFAULT_SIZE = NumericExpression.of(.2),
            DEFAULT_STEP = NumericExpression.of(.4);

    public RayTraceMechanic(ConfigObject config) {
        super(config);

        onTick = config.getScriptOrNull("tick");
        onHitEntity = config.getScriptOrNull("hit_entity");
        onHitBlock = config.getScriptOrNull("hit_block");

        ignorePassable = config.bool(false, "ignore_passable", "ip");
        neutral = config.bool(true, "neutral");
        offense = config.bool(true, "offense");
        rayTraceType = config.parse(RayTraceType.DEFAULT, Parsers.RAY_TRACE_TYPE, "mode", "m");

        range = config.numericExpr(DEFAULT_RANGE, "range", "rng", "length", "len", "distance", "dist");
        size = config.numericExpr(DEFAULT_SIZE, "size", "width", "wide");
        step = config.numericExpr(DEFAULT_STEP, "step_size", "step", "st", "ss");
    }

    @Override
    public void cast(SkillMetadata meta, Location source, Vector direction) {
        final double step = this.step.evaluate(meta);
        final double range = this.range.evaluate(meta);

        Validate.isTrue(range > 0, "Range must be strictly positive");
        Validate.isTrue(step > 0, "Step must be strictly positive (don't make it too low)");

        // Entity filter
        final RayTraceResult result = getResult(meta, source, direction, range);
        final double length = result == null ? range : result.getHitPosition().distance(source.toVector());

        if (onTick != null) for (double j = 0; j < length; j += step) {
            Location intermediate = source.clone().add(direction.clone().multiply(j));
            onTick.cast(meta.clone(source, intermediate, null));
        }

        if (result == null) return;

        Location hitPosition = result.getHitPosition().toLocation(source.getWorld());

        if (onHitBlock != null && result.getHitBlock() != null)
            onHitBlock.cast(meta.clone(source, hitPosition, null));

        if (onHitEntity != null && result.getHitEntity() != null)
            onHitEntity.cast(meta.clone(source, hitPosition, result.getHitEntity()));
    }

    private RayTraceResult getResult(SkillMetadata meta, Location source, Vector direction, double range) {

        // Blocks only
        if (rayTraceType == RayTraceType.BLOCKS)
            return source.getWorld().rayTraceBlocks(source, direction, range, FluidCollisionMode.NEVER, ignorePassable);

        final Predicate<Entity> filter = neutral
                ? entity -> entity instanceof LivingEntity && !entity.equals(meta.getCaster().getPlayer())
                : entity -> MythicLib.plugin.getEntities().canInteract(meta.getCaster().getPlayer(), entity, offense ? InteractionType.OFFENSE_SKILL : InteractionType.SUPPORT_SKILL);
        final double size = this.size.evaluate(meta);
        Validate.isTrue(size >= 0, "Size must be positive or null");

        // Entities only
        if (rayTraceType == RayTraceType.ENTITIES)
            return source.getWorld().rayTraceEntities(source, direction, range, size, filter);

        // Both
        if (rayTraceType == RayTraceType.DEFAULT)
            return source.getWorld().rayTrace(source, direction, range, FluidCollisionMode.NEVER, ignorePassable, size, filter);

        throw new RuntimeException("Not implemented");
    }

    public enum RayTraceType {
        DEFAULT,
        BLOCKS,
        ENTITIES;
    }
}

