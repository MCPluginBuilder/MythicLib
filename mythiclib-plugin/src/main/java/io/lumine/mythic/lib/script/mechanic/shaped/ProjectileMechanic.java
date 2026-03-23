package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.type.DirectionMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ProjectileMechanic extends DirectionMechanic {
    private final NumericExpression speed, size, lifeSpan, step;
    private final Script onHitBlock, onHitEntity, onTick;
    private final boolean ignorePassable, stopOnBlock;

    /**
     * This determines if this skill is considered a support
     * or offense skill. Depending on the skill type, it changes
     * whether that skill can hit certain players.
     */
    private final boolean offense;

    /**
     * Maximum amount of enemies hit per that projectile
     */
    private final int hitLimit;

    private static final NumericExpression
            DEFAULT_SPEED = NumericExpression.of(10),
            DEFAULT_LIFE_SPAN = NumericExpression.of(60),
            DEFAULT_SIZE = NumericExpression.of(.2),
            DEFAULT_STEP = NumericExpression.of(.2);

    public ProjectileMechanic(ConfigObject config) {
        super(config);

        onTick = config.contains("tick") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("tick")) : null;
        onHitEntity = config.contains("hit_entity") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("hit_entity")) : null;
        onHitBlock = config.contains("hit_block") ? MythicLib.plugin.getSkills().getScriptOrThrow(config.getString("hit_block")) : null;
        ignorePassable = config.getBoolean("ignore_passable", false);
        offense = config.getBoolean("offense", true);
        hitLimit = config.getInt("hits", 1);
        stopOnBlock = config.getBoolean("stop_on_block", true);

        speed = config.numericExpr(DEFAULT_SPEED, "speed");
        size = config.numericExpr(DEFAULT_SIZE, "size");
        step = config.numericExpr(DEFAULT_STEP, "step");
        lifeSpan = config.numericExpr(DEFAULT_LIFE_SPAN, "life_span");
    }

    @Override
    public void cast(SkillMetadata meta, Location source, Vector dir) {

        // Basic verifications
        final double speed = this.speed.evaluate(meta);
        Validate.isTrue(speed > 0, "Speed must be strictly positive");

        final double projectileSize = size.evaluate(meta);
        Validate.isTrue(projectileSize >= 0, "Size must be positive or null");

        final double projLifeSpan = lifeSpan.evaluate(meta);
        Validate.isTrue(projLifeSpan > 0, "Life span must be strictly positive (don't make it too low)");

        new BukkitRunnable() {

            // Register for hit entities
            final List<Integer> hitEntities = hitLimit > 1 ? new ArrayList<>() : null;

            // Direction is normalized
            final Vector dr = dir.clone().multiply(.05 * speed);
            final double dl = dr.length(), smallest_d = step.evaluate(meta);

            // Location being incremented every second
            Location current = source.clone();

            // Projectile time counter
            int counter = 0;

            public void run() {
                if (counter++ >= projLifeSpan) {
                    cancel();
                    return;
                }

                current.add(dr);

                Predicate<Entity> filter = entity -> MythicLib.plugin.getEntities().canInteract(meta.getCaster().getPlayer(), entity, offense ? InteractionType.OFFENSE_SKILL : InteractionType.SUPPORT_SKILL);
                RayTraceResult result = onHitBlock != null ? current.getWorld().rayTrace(current, dir, dl, FluidCollisionMode.NEVER, ignorePassable, projectileSize, filter) : current.getWorld().rayTraceEntities(current, dir, dl, projectileSize, filter);

                if (onTick != null) for (double j = 0; j < dl; j += smallest_d) {
                    Location intermediate = current.clone().add(dir.clone().multiply(j));
                    onTick.cast(meta.clone(source, intermediate, null));
                }

                if (result == null) return;

                if (onHitBlock != null && result.getHitBlock() != null) {
                    onHitBlock.cast(meta.clone(source, result.getHitPosition().toLocation(current.getWorld()), null));
                    if (stopOnBlock) cancel();
                }

                if (onHitEntity != null && result.getHitEntity() != null) {

                    // Register entity hit
                    if (hitEntities != null && (hitEntities.size() + 1) < hitLimit) {
                        hitEntities.add(result.getHitEntity().getEntityId());
                    } else cancel();

                    onHitEntity.cast(meta.clone(source, result.getHitPosition().toLocation(current.getWorld()), result.getHitEntity()));
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
