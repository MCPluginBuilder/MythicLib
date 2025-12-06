package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.DirectionMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

@Deprecated
@MechanicMetadata
public class RayTraceEntitiesMechanic extends DirectionMechanic {
    private final NumericExpression range, size, step;
    private final Script onHit, onTick;

    public RayTraceEntitiesMechanic(ConfigObject config) {
        super(config);

        onTick = config.contains("tick") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("tick")) : null;
        onHit = config.contains("hit_entity") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("hit_entity")) : null;

        range = config.numericExpr(NumericExpression.of(50), "range");
        size = config.numericExpr(NumericExpression.of(.2), "size");
        step = config.numericExpr(NumericExpression.of(.4), "step");
    }

    @Override
    public void cast(SkillMetadata meta, Location source, Vector dir) {
        final double range = this.range.evaluate(meta);
        final double size = this.size.evaluate(meta);
        final double step = this.step.evaluate(meta);

        Validate.isTrue(range > 0, "Range must be strictly positive");
        Validate.isTrue(size >= 0, "Size must be positive or null");
        Validate.isTrue(step > 0, "Step must be strictly positive (don't make it too low)");

        final Predicate<Entity> filter = entity -> entity instanceof LivingEntity && !entity.equals(meta.getCaster().getPlayer());
        final RayTraceResult result = source.getWorld().rayTraceEntities(source, dir, range, size, filter);
        final double length = result == null ? range : result.getHitPosition().distance(source.toVector());

        if (onTick != null)
            for (double j = 0; j < length; j += step) {
                Location intermediate = source.clone().add(dir.clone().multiply(j));
                onTick.cast(meta.clone(source, intermediate, null));
            }

        if (result != null && onHit != null && result.getHitEntity() != null) {
            Location hitPosition = result.getHitPosition().toLocation(source.getWorld());
            //SkillOrientation orientation = new SkillOrientation(hitPosition, dir);
            onHit.cast(meta.clone(source, hitPosition, result.getHitEntity()));
        }
    }
}