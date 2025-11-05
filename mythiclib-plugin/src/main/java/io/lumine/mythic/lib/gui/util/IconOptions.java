package io.lumine.mythic.lib.gui.util;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.editable.item.ItemOptions;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.util.config.YamlUtils;
import io.lumine.mythic.lib.version.ServerVersion;
import io.lumine.mythic.lib.version.VersionUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Bridges display options between MythicLib custom UIs and
 * anything else that does not make use of the custom UIs.
 *
 * @see PhysicalItem
 */
public class IconOptions {

    @Nullable
    private final Material material;

    @Nullable
    private final Integer customModelDataInt;

    @Nullable
    private final String customModelDataString;

    @Nullable
    private final Float customModelDataFloat;

    @Nullable
    private final ItemFlag[] itemFlags;

    private final Boolean hideTooltip, fakeAttribute, unbreakable;

    @Nullable
    private final NamespacedKey itemModel;

    @Nullable
    private final String skullTexture, tooltipStyle;

    public IconOptions() {
        this(null, (Integer) null);
    }

    public IconOptions(@Nullable Material material) {
        this(material, (Integer) null);
    }

    public IconOptions(@Nullable Material material, @Nullable Integer customModelDataInt) {
        this(material, customModelDataInt, null, null, null, null, null, null, null, null, null);
    }

    public IconOptions(@Nullable Material material, @Nullable String customModelDataString) {
        this(material, null, customModelDataString, null, null, null, null, null, null, null, null);
    }

    public IconOptions(@Nullable Material material,
                       @Nullable Integer customModelDataInt,
                       @Nullable String customModelDataString,
                       @Nullable Float customModelDataFloat,
                       @Nullable NamespacedKey itemModel,
                       @Nullable String skullTexture,
                       @Nullable String tooltipStyle,
                       @Nullable ItemFlag[] itemFlags,
                       @Nullable Boolean hideTooltip,
                       @Nullable Boolean fakeAttribute,
                       @Nullable Boolean unbreakable) {
        this.material = material;
        this.customModelDataInt = customModelDataInt;
        this.customModelDataString = customModelDataString;
        this.customModelDataFloat = customModelDataFloat;
        this.itemModel = itemModel;
        this.skullTexture = skullTexture;
        this.tooltipStyle = tooltipStyle;
        this.itemFlags = itemFlags;
        this.hideTooltip = hideTooltip;
        this.fakeAttribute = fakeAttribute;
        this.unbreakable = unbreakable;
    }

    @NotNull
    public Material getMaterialElse(@NotNull Material fallback) {
        return material == null ? fallback : material;
    }

    public void applyToItemMeta(@NotNull ItemMeta meta) {

        // Custom model data integer
        if (customModelDataInt != null) meta.setCustomModelData(customModelDataInt);

        // Custom model data component 1.21.4+
        final var hasCustomModelData = customModelDataString != null || customModelDataFloat != null;
        if (hasCustomModelData && ServerVersion.get().isAbove(1, 21, 4)) {
            CustomModelDataComponent comp = meta.getCustomModelDataComponent();

            if (customModelDataString != null) comp.setStrings(Collections.singletonList(customModelDataString));
            if (customModelDataFloat != null) comp.setFloats(Collections.singletonList(customModelDataFloat));

            meta.setCustomModelDataComponent(comp);
        }

        // Item model 1.21.2+
        if (itemModel != null && ServerVersion.get().isAbove(1, 21, 2)) meta.setItemModel(itemModel);

        // Skull texture
        if (skullTexture != null && meta instanceof SkullMeta)
            UtilityMethods.setTextureValue((SkullMeta) meta, skullTexture);

        // Tooltip style
        if (tooltipStyle != null && ServerVersion.get().isAbove(1, 21, 2))
            meta.setTooltipStyle(NamespacedKey.fromString(tooltipStyle));

        // Item flags
        if (itemFlags != null) meta.addItemFlags(itemFlags);

        // Hide tooltip
        if (hideTooltip != null && ServerVersion.get().isAbove(1, 20, 5)) meta.setHideTooltip(hideTooltip);

        // Fake attribute
        if (fakeAttribute != null && fakeAttribute) VersionUtils.addEmptyAttributeModifier(meta);

        // Unbreakable
        if (unbreakable != null) meta.setUnbreakable(unbreakable);
    }

    @NotNull
    public ItemStack toItemStack() {
        final var stack = new ItemStack(Objects.requireNonNullElse(material, Material.BARRIER));
        final var meta = stack.getItemMeta();
        applyToItemMeta(meta);
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public String toString() {
        return "IconOptions{" +
                "material=" + material +
                ", customModelDataInt=" + customModelDataInt +
                ", customModelDataString='" + customModelDataString + "'" +
                ", customModelDataFloat=" + customModelDataFloat +
                ", itemModel=" + itemModel +
                ", skullTexture='" + skullTexture + "'" +
                ", ..." +
                '}';
    }

    private static <T> T fallback(T primary, T fallback) {
        return primary != null ? primary : fallback;
    }

    @NotNull
    public IconOptions combine(@NotNull IconOptions fallback) {
        return new IconOptions(
                fallback(this.material, fallback.material),
                fallback(this.customModelDataInt, fallback.customModelDataInt),
                fallback(this.customModelDataString, fallback.customModelDataString),
                fallback(this.customModelDataFloat, fallback.customModelDataFloat),
                fallback(this.itemModel, fallback.itemModel),
                fallback(this.skullTexture, fallback.skullTexture),
                fallback(this.tooltipStyle, fallback.tooltipStyle),
                fallback(this.itemFlags, fallback.itemFlags),
                fallback(this.hideTooltip, fallback.hideTooltip),
                fallback(this.fakeAttribute, fallback.fakeAttribute),
                fallback(this.unbreakable, fallback.unbreakable));
    }

    //region Static methods

    public static final IconOptions EMPTY = new IconOptions(null, (Integer) null);

    @Nullable
    private static ItemFlag[] parseItemFlagArray(@Nullable List<String> list) {
        if (list == null) return null;

        final var result = new ItemFlag[list.size()];
        for (var i = 0; i < list.size(); i++)
            result[i] = UtilityMethods.prettyValueOf(ItemFlag::valueOf, list.get(i), "No item flag with ID '%s'");
        return result;
    }

    @NotNull
    public static IconOptions from(Object object) {

        // [Backwards compatibility]
        if (object instanceof String) {
            final var split = ((String) object).split("[:.,]");
            final var mat = Material.valueOf(UtilityMethods.enumName(split[0]));
            final var modelData = split.length == 1 ? 0 : Integer.parseInt(split[1]);
            return new IconOptions(mat, modelData);
        }

        // Read from config
        if (object instanceof ConfigurationSection) {
            final var config = (ConfigurationSection) object;
            final var rawMaterial = YamlUtils.getString(config, "item", "material");
            final var material = rawMaterial == null ? null : UtilityMethods.prettyValueOf(Material::valueOf, rawMaterial, "Could not find material with ID '%s'");

            // Mix of backwards compatibility and trying to be flexible
            // Inevitably messy so it's better to support everything even if it
            // takes 1 age of the universe to load one single item from config
            final var customModelDataInt = YamlUtils.getInteger(config, "custom_model_data", "custom-model-data", "model-data", "cmd", "model_data");
            final var customModelDataString = YamlUtils.getString(config, "custom_model_data_string", "custom-model-data-string", "cmd-string", "cmd_string", "cmds");
            final var customModelDataFloat = YamlUtils.getFloatObj(config, "custom_model_data_float", "custom-model-data-float", "cmd-float", "cmd_float", "cmdf");
            final var itemModelRaw = YamlUtils.getString(config, "model", "item_model", "item-model");
            final var itemModel = itemModelRaw == null || itemModelRaw.isEmpty() ? null : NamespacedKey.fromString(itemModelRaw);
            final var skullTexture = YamlUtils.getString(config, "texture", "skull_texture", "skull-texture");
            final var tooltipStyle = YamlUtils.getString(config, "tooltip", "tooltip_style", "tooltip-style");
            final var itemFlags = YamlUtils.getBoolean(config, "hide-flags", "hide_flags") ? ItemFlag.values() : parseItemFlagArray(YamlUtils.getStringList(config, "item_flags", "item-flags"));
            final var hideTooltip = YamlUtils.getBooleanObj(config, "hide_tooltip", "hide-tooltip");
            final var fakeAttribute = YamlUtils.getBooleanObj(config, "fake_attribute_modifier", "fake-attribute-modifier");
            final var unbreakable = YamlUtils.getBooleanObj(config, "unbreakable");

            return new IconOptions(material, customModelDataInt, customModelDataString, customModelDataFloat, itemModel, skullTexture, tooltipStyle, itemFlags, hideTooltip, fakeAttribute, unbreakable);
        }

        throw new IllegalArgumentException("Could not read icon");
    }

    //endregion

    //region Deprecated

    @Nullable
    @Deprecated
    public Material getMaterial() {
        return material;
    }

    @Nullable
    @Deprecated
    public Integer getCustomModelDataInt() {
        return customModelDataInt;
    }

    @Nullable
    @Deprecated
    public String getCustomModelDataString() {
        return customModelDataString;
    }

    @Nullable
    @Deprecated
    public NamespacedKey getItemModel() {
        return itemModel;
    }

    @Nullable
    @Deprecated
    public String getSkullTexture() {
        return skullTexture;
    }

    /**
     * Does not support custom model data floats, tooltip style, fake attribute,
     * item flags, hide tooltip, unbreakable, skull texture.
     *
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    public static IconOptions from(@Nullable ItemStack from) {

        Material mat = from != null ? from.getType() : null;
        Integer customModelData = null;
        String customModelDataString = null;
        NamespacedKey itemModel = null;

        if (from != null && from.hasItemMeta()) {
            ItemMeta meta = from.getItemMeta();

            // Int custom model data
            if (meta.hasCustomModelData()) customModelData = meta.getCustomModelData();

            // String custom model data
            if (ServerVersion.get().isAbove(1, 21, 4)) {
                var cmd = meta.getCustomModelDataComponent();
                var stringList = cmd.getStrings();
                if (stringList != null && !stringList.isEmpty()) customModelDataString = stringList.getFirst();
            }

            // Item model 1.21.2+
            if (ServerVersion.get().isAbove(1, 21, 2)) {
                var model = meta.getItemModel();
                if (model != null) itemModel = model;
            }

            // TODO support for other options? This method is deprecated so no need anyways
        }

        return new IconOptions(mat, customModelData, customModelDataString, null, itemModel, null, null, null, null, null, null);
    }

    //endregion
}
