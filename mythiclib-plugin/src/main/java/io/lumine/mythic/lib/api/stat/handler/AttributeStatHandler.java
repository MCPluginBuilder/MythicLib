package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.VersionUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class AttributeStatHandler extends StatHandler {
    protected final Attribute attribute;
    private final Material material;
    private final String description;
    private final double playerDefaultBase;

    protected static final NamespacedKey ATTRIBUTE_KEY = new NamespacedKey(MythicLib.plugin, "main");
    protected static final double EPSILON = .0001;

    /**
     * Statistics like Atk Damage, Atk Speed, Max Health...
     * which are based on vanilla player attributes.
     *
     * @param config      The root configuration file
     * @param stat        The stat identifier
     * @param material    For usage, see {@link io.lumine.mythic.lib.gui.AttributeExplorer}
     * @param description For usage, see {@link io.lumine.mythic.lib.gui.AttributeExplorer}
     */
    public AttributeStatHandler(ConfigurationSection config,
                                @NotNull String stat,
                                double playerDefaultBase,
                                @NotNull Material material,
                                @NotNull String description) {
        super(config, stat);

        this.attribute = Attributes.adapt(stat);
        this.material = material;
        this.description = description;
        this.playerDefaultBase = playerDefaultBase;

        // Force update on login
        this.forceUpdate = true;
    }

    @Override
    public void runUpdate(@NotNull StatInstance instance) {
        final AttributeInstance attrIns = instance.getMap().getData().getPlayer().getAttribute(attribute);
        removeModifiers(attrIns);

        final double vanillaBase = instance.getMap().getData().getPlayer().getAttribute(attribute).getBaseValue();
        final double mmoFinal = clampValue(instance.getFilteredTotal(vanillaBase + this.baseValue, EquipmentSlot.MAIN_HAND::isCompatible));
        final double difference = mmoFinal - vanillaBase;

        /*
         * Only add an attribute modifier if the very final stat
         * value is different from the main one to save map updates.
         */
        if (Math.abs(difference) > EPSILON)
            attrIns.addModifier(VersionUtils.attrMod(ATTRIBUTE_KEY, difference, AttributeModifier.Operation.ADD_NUMBER));
    }

    @Override
    public double getBaseValue(@NotNull StatInstance instance) {
        return this.baseValue + instance.getMap().getData().getPlayer().getAttribute(attribute).getBaseValue();
    }

    @Override
    public double getFinalValue(@NotNull StatInstance instance) {
        return instance.getMap().getData().getPlayer().getAttribute(attribute).getValue();
    }

    protected void removeModifiers(@NotNull AttributeInstance ins) {
        for (AttributeModifier mod : ins.getModifiers())
            if (VersionUtils.matches(mod, ATTRIBUTE_KEY)) ins.removeModifier(mod);
    }

    @NotNull
    public Attribute getAttribute() {
        return attribute;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    public double getPlayerDefaultBase() {
        return playerDefaultBase;
    }
}
