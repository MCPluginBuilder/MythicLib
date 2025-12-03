package io.lumine.mythic.lib.api.stat.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.gui.builtin.AttributeExplorer;
import io.lumine.mythic.lib.util.lang3.Validate;
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
    protected final double playerDefaultBase;

    protected static final NamespacedKey ATTRIBUTE_KEY = new NamespacedKey(MythicLib.plugin, "main");
    protected static final double EPSILON = .0001;

    /**
     * Statistics like Atk Damage, Atk Speed, Max Health...
     * which are based on vanilla player attributes.
     *
     * @param config      The root configuration file
     * @param stat        The stat identifier
     * @param material    For usage, see {@link AttributeExplorer}
     * @param description For usage, see {@link AttributeExplorer}
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
        this.updateOnLogin = true;

        addUpdateListener(this::updateAttributeModifierValue);
    }

    private void updateAttributeModifierValue(@NotNull StatInstance instance) {
        Validate.isTrue(instance.getStat().equals(stat), "Attribute stat handler of " + this.stat + " got stat " + instance.getStat());

        // Clear previous modifiers from Bukkit attribute instance
        final var attributeInstance = instance.getMap().getData().getPlayer().getAttribute(attribute);
        assert attributeInstance != null;
        removeModifiers(attributeInstance);

        final var mmoFinal = instance.getTotal(this.playerDefaultBase + this.baseValue, EquipmentSlot.MAIN_HAND);
        final var difference = mmoFinal - this.playerDefaultBase;

        // Only register attribute modifier if absolutely necessary
        if (Math.abs(difference) > EPSILON)
            attributeInstance.addModifier(VersionUtils.attrMod(ATTRIBUTE_KEY, difference, AttributeModifier.Operation.ADD_NUMBER));
    }

    @Override
    public double getBaseValue(@NotNull StatInstance instance) {
        return this.baseValue + instance.getMap().getData().getPlayer().getAttribute(attribute).getBaseValue();
    }

    @Override
    public double getPlayerDefaultBase() {
        return playerDefaultBase;
    }

    @Override
    public double getFinalValue(@NotNull StatInstance instance) {
        return instance.getMap().getData().getPlayer().getAttribute(attribute).getValue();
    }

    protected static void removeModifiers(@NotNull AttributeInstance ins) {
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
}
