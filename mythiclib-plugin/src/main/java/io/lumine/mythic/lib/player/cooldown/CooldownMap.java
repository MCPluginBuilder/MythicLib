package io.lumine.mythic.lib.player.cooldown;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.player.PlayerDataMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CooldownMap extends PlayerDataMap {
    private final Map<String, CooldownInfo> map = new HashMap<>();

    /**
     * Sets current cooldown to the maximum value
     * of the current and input cooldown values.
     *
     * @param obj      The skill or action
     * @param cooldown Initial skill or action cooldown
     * @return The newly registered cooldown info
     */
    @NotNull
    public CooldownInfo applyCooldown(@NotNull CooldownObject obj, double cooldown) {
        return applyCooldown(obj.getCooldownPath(), cooldown);
    }

    /**
     * Sets current cooldown to the maximum value
     * of the current and input cooldown values.
     *
     * @param path     The skill or action path, must be completely unique
     * @param cooldown Initial skill or action cooldown, in seconds
     * @return The newly registered cooldown info
     */
    @NotNull
    public CooldownInfo applyCooldown(@NotNull String path, double cooldown) {
        tryFlush(); // Only flush when adding new cooldowns

        final var key = UtilityMethods.enumName(path);
        return map.compute(key, (k, current) -> {
            if (current == null) return new CooldownInfo(cooldown);
            if (current.getRemaining() >= cooldown * 1000) return current;
            return new CooldownInfo(cooldown);
        });
    }

    /**
     * @return Finds the cooldown info for a specific action or skill
     */
    @Nullable
    public CooldownInfo getInfo(@NotNull CooldownObject obj) {
        return getInfo(obj.getCooldownPath());
    }

    /**
     * @return Finds the cooldown info for a specific action or skill
     */
    @Nullable
    public CooldownInfo getInfo(@NotNull String path) {
        return map.get(UtilityMethods.enumName(path));
    }

    /**
     * @param obj The skill or action
     * @return Retrieves the remaining cooldown in seconds
     */
    public double getCooldown(@NotNull CooldownObject obj) {
        return getCooldown(obj.getCooldownPath());
    }

    /**
     * @param path The skill or action path, must be completely unique
     * @return Retrieves the remaining cooldown in seconds
     */
    public double getCooldown(@NotNull String path) {
        final @Nullable var info = map.get(UtilityMethods.enumName(path));
        return info == null ? 0 : (double) info.getRemaining() / 1000;
    }

    /**
     * @param obj The skill or action
     * @return If the mechanic can be used by the player
     */
    public boolean isOnCooldown(@NotNull CooldownObject obj) {
        return isOnCooldown(obj.getCooldownPath());
    }

    /**
     * @param path The skill or action path, must be completely unique
     * @return If the mechanic can be used by the player
     */
    public boolean isOnCooldown(@NotNull String path) {
        final @Nullable var found = map.get(UtilityMethods.enumName(path));
        return found != null && !found.hasEnded();
    }

    /**
     * Entirely resets a cooldown for given action.
     *
     * @param obj The skill or action
     */
    public void resetCooldown(@NotNull CooldownObject obj) {
        resetCooldown(obj.getCooldownPath());
    }

    /**
     * Entirely resets a cooldown for given path.
     *
     * @param path The skill or action path, must be completely unique
     */
    public void resetCooldown(@NotNull String path) {
        map.remove(UtilityMethods.enumName(path));
    }

    @NotNull
    public Set<String> getCooldownKeys() {
        return this.map.keySet();
    }

    public void clearAllCooldowns() {
        map.clear();
    }

    //region Map flushing

    private long nextFlush = System.currentTimeMillis() + FLUSH_INTERVAL;

    private static final long FLUSH_INTERVAL = 60 * 1000;

    private void tryFlush() {
        if (System.currentTimeMillis() < nextFlush) return;

        nextFlush = System.currentTimeMillis() + FLUSH_INTERVAL;
        map.values().removeIf(CooldownInfo::hasEnded);
    }

    //endregion
}
