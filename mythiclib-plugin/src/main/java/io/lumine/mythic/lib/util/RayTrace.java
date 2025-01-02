package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.version.VParticle;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class RayTrace {

    // Ray trace result
    private final LivingEntity hitEntity;
    private final double distanceTraveled;

    // Initial data
    private final Location initialLocation;
    private final Vector initialDirection;

    // Static ray trace options
    private static final FluidCollisionMode FLUID_COLLISION_MODE = FluidCollisionMode.NEVER;
    private static final boolean IGNORE_PASSABLE_BLOCKS = true;
    private static final double RAY_SIZE = .2;

    public RayTrace(Player player, double range, Predicate<Entity> filter) {
        this(player.getEyeLocation(), player.getEyeLocation().getDirection(), range, filter);
    }

    public RayTrace(Player player, Vector direction, double range, Predicate<Entity> filter) {
        this(player.getEyeLocation(), direction, range, filter);
    }

    /**
     * Casts a ray trace and saves the data used by MMOItems or MMOCore.
     * This only saves the distance travelled and potentially the entity
     * hit (used by MI staffs and other untargeted weapons).
     *
     * @param player Player casting ray
     * @param hand   The ray trace will be cast from that particular hand
     * @param range  The maximum ray cast range
     * @param filter Filters entities selected by the ray cast
     */
    public RayTrace(Player player, EquipmentSlot hand, double range, Predicate<Entity> filter) {

        // Calculate initial location
        Validate.isTrue(hand.isHand(), "Not a hand equipment slot");
        final double a = Math.toRadians(player.getEyeLocation().getYaw() + 90 + 45 * (hand == EquipmentSlot.MAIN_HAND ? 1 : -1));
        initialLocation = player.getLocation().add(Math.cos(a) * .5, 1.5, Math.sin(a) * .5);

        // Ray trace
        initialDirection = player.getEyeLocation().getDirection();
        @Nullable final RayTraceResult result = initialLocation.getWorld().rayTrace(initialLocation, initialDirection, range, FLUID_COLLISION_MODE, IGNORE_PASSABLE_BLOCKS, RAY_SIZE, filter);
        hitEntity = result == null || result.getHitEntity() == null ? null : (LivingEntity) result.getHitEntity();
        distanceTraveled = result == null ? range : result.getHitPosition().distance(initialLocation.toVector());
    }

    /**
     * Casts a ray trace and saves the data used by MMOItems or MMOCore.
     * This only saves the distance travelled and potentially the entity
     * hit (used by MI staffs and other untargeted weapons).
     *
     * @param loc       Initial ray cast location
     * @param direction Normalized direction
     * @param range     The maximum ray cast range
     * @param filter    Filters entities selected by the ray cast
     */
    public RayTrace(Location loc, Vector direction, double range, Predicate<Entity> filter) {
        @Nullable final RayTraceResult result = loc.getWorld().rayTrace(loc, direction, range, FLUID_COLLISION_MODE, IGNORE_PASSABLE_BLOCKS, RAY_SIZE, filter);
        hitEntity = result == null || result.getHitEntity() == null ? null : (LivingEntity) result.getHitEntity();
        distanceTraveled = result == null ? range : result.getHitPosition().distance(loc.toVector());
        initialDirection = direction;
        initialLocation = loc;
    }

    public boolean hasHit() {
        return hitEntity != null;
    }

    @Nullable
    public LivingEntity getHit() {
        return hitEntity;
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void draw(double step, Color color) {
        draw(step, loc -> loc.getWorld().spawnParticle(VParticle.REDSTONE.get(), loc, 0, new Particle.DustOptions(color, 1)));
    }

    public void draw(double step, Consumer<Location> tick) {
        for (double d = 0; d < distanceTraveled; d += step)
            tick.accept(initialLocation.clone().add(initialDirection.clone().multiply(d)));
    }
}
