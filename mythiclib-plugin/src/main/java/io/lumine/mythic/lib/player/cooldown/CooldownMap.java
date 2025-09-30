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
    public CooldownInfo applyCooldown(CooldownObject obj, double cooldown) {
        return applyCooldown(obj.getCooldownPath(), cooldown);
    }

    /**
     * Sets current cooldown to the maximum value
     * of the current and input cooldown values.
     *
     * @param path     The skill or action path, must be completely unique
     * @param cooldown Initial skill or action cooldown
     * @return The newly registered cooldown info
     */
    public CooldownInfo applyCooldown(String path, double cooldown) {
        final String key = UtilityMethods.enumName(path);
        @Nullable CooldownInfo current = map.get(key);
        if (current != null && current.getRemaining() >= cooldown * 1000) return current;

        current = new CooldownInfo(cooldown);
        map.put(key, current);
        return current;
    }

    /**
     * @return Finds the cooldown info for a specific action or skill
     */
    @Nullable
    public CooldownInfo getInfo(CooldownObject obj) {
        return getInfo(obj.getCooldownPath());
    }

    /**
     * @return Finds the cooldown info for a specific action or skill
     */
    @Nullable
    public CooldownInfo getInfo(String path) {
        return map.get(UtilityMethods.enumName(path));
    }

    /**
     * @param obj The skill or action
     * @return Retrieves the remaining cooldown in seconds
     */
    public double getCooldown(CooldownObject obj) {
        return getCooldown(obj.getCooldownPath());
    }

    /**
     * @param path The skill or action path, must be completely unique
     * @return Retrieves the remaining cooldown in seconds
     */
    public double getCooldown(String path) {
        final @Nullable CooldownInfo info = map.get(UtilityMethods.enumName(path));
        return info == null ? 0 : (double) info.getRemaining() / 1000;
    }

    /**
     * @param obj The skill or action
     * @return If the mechanic can be used by the player
     */
    public boolean isOnCooldown(CooldownObject obj) {
        return isOnCooldown(obj.getCooldownPath());
    }

    /**
     * @param path The skill or action path, must be completely unique
     * @return If the mechanic can be used by the player
     */
    public boolean isOnCooldown(String path) {
        final @Nullable CooldownInfo found = map.get(UtilityMethods.enumName(path));
        return found != null && !found.hasEnded();
    }

    /**
     * Entirely resets a cooldown for given action.
     *
     * @param obj The skill or action
     */
    public void resetCooldown(CooldownObject obj) {
        resetCooldown(obj.getCooldownPath());
    }

    /**
     * Entirely resets a cooldown for given path.
     *
     * @param path The skill or action path, must be completely unique
     */
    public void resetCooldown(String path) {
        map.remove(UtilityMethods.enumName(path));
    }

    @NotNull
    public Set<String> getCooldownKeys() {
        return this.map.keySet();
    }

    public void clearAllCooldowns() {
        map.clear();
    }
}
