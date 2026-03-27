package io.lumine.mythic.lib.player.resource;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.version.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Resources {

    //region Health

    @Nullable
    private static HealthUpdateEventSupplier<?> healthUpdateEventSupplier;

    public static void setResourceEventCaller(@Nullable HealthUpdateEventSupplier<?> eventSupplier) {
        healthUpdateEventSupplier = eventSupplier;
    }

    public static boolean setHealth(@NotNull LivingEntity entity, double newValue) {
        return setHealth(entity, newValue, ResourceUpdateReason.OTHER);
    }

    public static boolean setHealth(@NotNull LivingEntity entity, double newValue, @NotNull ResourceUpdateReason updateReason) {
        // TODO replace by PlayerCasterMetadata invalidation check
        if (entity.isDead() || (entity instanceof Player && UtilityMethods.isInvalidated((Player) entity))) return false;

        final var maxValue = entity.getAttribute(Attributes.MAX_HEALTH).getValue();
        final var oldValue = entity.getHealth();
        newValue = Math.max(0, Math.min(newValue, maxValue));
        if (oldValue == newValue) return false;

        // Don't call event for that.
        if (entity instanceof Player && updateReason.callsEvent() && healthUpdateEventSupplier != null) {
            var bukkitCalled = healthUpdateEventSupplier.onHealthUpdate((Player) entity, oldValue, newValue, updateReason);
            Bukkit.getPluginManager().callEvent(bukkitCalled);
            if (bukkitCalled.isCancelled()) return false;
            newValue = bukkitCalled.getNewAmount();
        }

        // Use updated amount from event
        entity.setHealth(Math.max(0, Math.min(newValue, maxValue)));
        return true;
    }

    public static boolean heal(@NotNull LivingEntity entity, double healed) {
        return setHealth(entity, entity.getHealth() + healed, ResourceUpdateReason.OTHER);
    }

    public static boolean heal(@NotNull LivingEntity entity, double healed, @NotNull ResourceUpdateReason reason) {
        return setHealth(entity, entity.getHealth() + healed, reason);
    }

    //endregion

    private static final float MAX_SATURATION = 20;

    /**
     * @param player     Player to heal
     * @param saturation Saturation amount
     */
    public static boolean saturate(@NotNull Player player, double saturation) {
        return saturate(player, saturation, true);
    }

    /**
     * @param player         Player to heal
     * @param saturation     Saturation amount
     * @param allowNegatives Should negative amounts be ignored
     */
    public static boolean saturate(@NotNull Player player, double saturation, boolean allowNegatives) {
        if (saturation == 0) return false;
        if (saturation < 0 && !allowNegatives) return false;

        player.setSaturation(Math.max(0, Math.min(MAX_SATURATION, player.getSaturation() + (float) saturation)));
        return true;
    }

    private static final int MAX_FOOD_LEVEL = 20;

    /**
     * @param player Player to heal
     * @param feed   Food amount
     */
    public static boolean feed(@NotNull Player player, int feed) {
        return feed(player, feed, true);
    }

    /**
     * @param player         Player to heal
     * @param feed           Food amount
     * @param allowNegatives Should negative amounts be ignored
     */
    public static boolean feed(@NotNull Player player, int feed, boolean allowNegatives) {
        if (feed == 0) return false;
        if (feed < 0 && !allowNegatives) return false;

        player.setFoodLevel(Math.max(Math.min(MAX_FOOD_LEVEL, player.getFoodLevel() + feed), 0));
        return true;
    }
}
