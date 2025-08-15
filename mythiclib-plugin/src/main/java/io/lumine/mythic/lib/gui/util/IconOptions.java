package io.lumine.mythic.lib.gui.util;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.version.ServerVersion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;

/**
 * Bridges display options between MythicLib custom UIs and
 * anything else that does not make use of the custom UIs.
 * <p>
 * TODO include skull texture for player skulls + hide tooltip/attributes...?
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
    private final NamespacedKey itemModel;

    private final String skullTexture;

    public IconOptions(@Nullable Material material) {
        this(material, null, null, null, null);
    }

    public IconOptions(@Nullable Material material, @Nullable Integer customModelDataInt) {
        this(material, customModelDataInt, null, null, null);
    }

    public IconOptions(@Nullable Material material,
                       @Nullable Integer customModelDataInt,
                       @Nullable String customModelDataString,
                       @Nullable NamespacedKey itemModel,
                       @Nullable String skullTexture) {
        this.material = material;
        this.customModelDataInt = customModelDataInt;
        this.customModelDataString = customModelDataString;
        this.itemModel = itemModel;
        this.skullTexture = skullTexture;
    }

    @Nullable
    public Material getMaterial() {
        return material;
    }

    @Nullable
    public Integer getCustomModelDataInt() {
        return customModelDataInt;
    }

    @Nullable
    public String getCustomModelDataString() {
        return customModelDataString;
    }

    @Nullable
    public NamespacedKey getItemModel() {
        return itemModel;
    }

    @Nullable
    public String getSkullTexture() {
        return skullTexture;
    }

    @NotNull
    public Material getMaterialElse(@NotNull Material fallback) {
        return material == null ? fallback : material;
    }

    public void applyToItemMeta(@NotNull ItemMeta meta) {

        // Custom model data integer
        if (customModelDataInt != null) meta.setCustomModelData(customModelDataInt);

        // Custom model data string 1.21.4+
        if (customModelDataString != null && ServerVersion.get().isAbove(1, 21, 4)) {
            CustomModelDataComponent comp = meta.getCustomModelDataComponent();
            comp.setStrings(Collections.singletonList(customModelDataString));
            meta.setCustomModelDataComponent(comp);
        }

        // Item model 1.21.2+
        if (itemModel != null && ServerVersion.get().isAbove(1, 21, 2)) meta.setItemModel(itemModel);

        // Skull texture
        if (skullTexture != null && meta instanceof SkullMeta)
            UtilityMethods.setTextureValue((SkullMeta) meta, skullTexture);
    }

    @NotNull
    public ItemStack toItemStack() {
        var stack = new ItemStack(Objects.requireNonNullElse(material, Material.BARRIER));
        var meta = stack.getItemMeta();
        applyToItemMeta(meta);
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public String toString() {
        return "IconOptions{" +
                "material=" + material +
                ", customModelDataInt=" + customModelDataInt +
                ", customModelDataString='" + customModelDataString + '\'' +
                ", itemModel=" + itemModel +
                '}';
    }

    @NotNull
    public IconOptions combine(@NotNull IconOptions fallback) {

        var material = this.material != null ? this.material : fallback.material;
        var customModelDataInt = this.customModelDataInt != null ? this.customModelDataInt : fallback.customModelDataInt;
        var customModelDataString = this.customModelDataString != null ? this.customModelDataString : fallback.customModelDataString;
        var itemModel = this.itemModel != null ? this.itemModel : fallback.itemModel;
        var skullTexture = this.skullTexture != null ? this.skullTexture : fallback.skullTexture;

        return new IconOptions(material, customModelDataInt, customModelDataString, itemModel, skullTexture);
    }

    //region Static methods

    public static final IconOptions EMPTY = new IconOptions(null, null, null, null, null);

    public static IconOptions from(@Nullable ItemStack from) {

        Material mat = from != null ? from.getType() : null;
        Integer customModelData = null;
        String customModelDataString = null;
        NamespacedKey itemModel = null;
        String skullTexture = null;

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

            // Skull texture
            // TODO
        }

        return new IconOptions(mat, customModelData, customModelDataString, itemModel, skullTexture);
    }

    @NotNull
    public static IconOptions from(Object object) {

        // [Backwards compatibility]
        if (object instanceof String) {
            final var split = ((String) object).split("[:.,]");
            final var mat = Material.valueOf(UtilityMethods.enumName(split[0]));
            final var modelData = split.length == 1 ? 0 : Integer.parseInt(split[1]);
            return new IconOptions(mat, modelData, null, null, null);
        }

        // Read from config
        if (object instanceof ConfigurationSection) {
            final var config = (ConfigurationSection) object;
            final var rawMaterial = config.getString("item", "BARRIER");
            final var material = UtilityMethods.prettyValueOf(Material::valueOf, rawMaterial, "Could not find material with ID '%s'");

            // [Backwards compatibility] MMOCore 'model-data'
            final var customModelDataInt = config.getInt("custom-model-data", config.getInt("model-data"));
            final var customModelDataString = config.getString("custom-model-data-string");
            final var itemModelRaw = config.getString("model", config.getString("item-model"));
            final @Nullable var itemModel = itemModelRaw == null || itemModelRaw.isEmpty() ? null : NamespacedKey.fromString(itemModelRaw);
            final var skullTexture = config.getString("texture", config.getString("skull-texture"));

            return new IconOptions(material, toInteger(customModelDataInt), customModelDataString, itemModel, skullTexture);
        }

        throw new IllegalArgumentException("Could not read icon");
    }

    @Nullable
    private static Integer toInteger(int primitive) {
        return primitive == 0 ? null : primitive;
    }

    //endregion
}
