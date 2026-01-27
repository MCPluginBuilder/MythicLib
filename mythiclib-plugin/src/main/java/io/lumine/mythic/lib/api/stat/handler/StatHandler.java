package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to handle complex stat behaviors, including updates
 * ran whenever a player stat changes or stat base values.
 *
 * @author jules
 */
public class StatHandler {
    protected final boolean hasMinValue, hasMaxValue;
    protected final double baseValue, minValue, maxValue;
    protected final DecimalFormat decimalFormat;
    protected final String stat;

    private final List<StatUpdateListener> updates = new ArrayList<>();
    @Nullable
    private ModifierEditor modifierEditor;

    /**
     * Should this stat force updates on player login
     */
    protected boolean updateOnLogin;

    /**
     * @param config Root stat handlers configuration file
     * @param stat   Unique string identifier of stat
     */
    public StatHandler(@NotNull ConfigurationSection config, @NotNull String stat) {
        this.stat = stat;
        final String[] splitBounds = (" " + config.getString("min-max-values." + this.stat, "=") + " ").split("=");
        Validate.isTrue(splitBounds.length == 2, "Could not find unique = separator symbol");
        final String cleanMin = splitBounds[0].replace(" ", "");
        final String cleanMax = splitBounds[1].replace(" ", "");
        hasMinValue = !cleanMin.isEmpty();
        hasMaxValue = !cleanMax.isEmpty();
        minValue = hasMinValue ? Double.parseDouble(cleanMin) : 0;
        maxValue = hasMaxValue ? Double.parseDouble(cleanMax) : 0;
        baseValue = config.getDouble("base-stat-value." + this.stat);
        decimalFormat = config.contains("decimal-format." + this.stat)
                ? MythicLib.plugin.getMMOConfig().newDecimalFormat(config.getString("decimal-format." + this.stat))
                : MythicLib.plugin.getMMOConfig().decimal;
    }

    public StatHandler(@NotNull String stat) {
        this.stat = stat;
        this.hasMinValue = false;
        this.hasMaxValue = false;
        this.minValue = 0;
        this.maxValue = 0;
        this.baseValue = 0;
        this.decimalFormat = MythicLib.plugin.getMMOConfig().decimal;
    }

    @NotNull
    public String getStat() {
        return stat;
    }

    @NotNull
    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    public void addUpdateListener(@NotNull StatUpdateListener listener) {
        Validate.notNull(listener, "Cannot register null consumer to stat updates");
        updates.add(listener);
    }

    public void setModifierEditor(@Nullable ModifierEditor modifierEditor) {
        this.modifierEditor = modifierEditor;
    }

    @Nullable
    public ModifierEditor getModifierEditor() {
        return modifierEditor;
    }

    public void delegateTo(@NotNull String stat) {
        addUpdateListener(ins -> ins.getMap().getInstance(stat).update());
    }

    public void runUpdates(@NotNull StatInstance instance) {
        for (var update : updates) update.onUpdate(instance);
    }

    /**
     * This is an import class for vanilla attribute based statistics like Max Health.
     * MythicLib can't use 20 as base stat value because this can be edited by other
     * plugins. It must retrieve the actual player's base attribute value
     *
     * @param instance Stat instance collecting the stat value
     * @return The player's base stat value
     */
    public double getBaseValue(@NotNull StatInstance instance) {
        return baseValue;
    }

    public double getPlayerDefaultBase() {
        return 0;
    }

    public boolean updateOnLogin() {
        return updateOnLogin;
    }

    /**
     * Used for attribute-based statistics. These stats already exist in Minecraft therefore
     * the final value doesn't match the value returned by {@link StatInstance#getTotal()}
     * but it is the same as {@link StatMap#getStat(String)}
     *
     * @param instance Stat instance collecting the stat value
     * @return The player's total stat value
     */
    public double getFinalValue(@NotNull StatInstance instance) {
        return instance.getTotal();
    }

    /**
     * This clips a stat value in the bounds configured in the config sections.
     *
     * @param clamped Value to be clipped
     */
    public double clampValue(double clamped) {
        if (hasMaxValue && clamped > maxValue) clamped = maxValue;
        if (hasMinValue && clamped < minValue) clamped = minValue;
        return clamped;
    }

    @Deprecated
    public boolean forcesUpdates() {
        return updateOnLogin;
    }
}
