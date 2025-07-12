package io.lumine.mythic.lib.gui.editable.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemOptions {
    private final int index;

    @Nullable
    private final Material material;

    @Nullable
    private final Integer customModelData;

    @Nullable
    private final String customModelDataString;

    private ItemOptions(int index, @Nullable Material material, @Nullable Integer customModelData, @Nullable String customModelDataString) {
        this.index = index;
        this.material = material;
        this.customModelData = customModelData;
        this.customModelDataString = customModelDataString;
    }

    public int index() {
        return index;
    }

    @NotNull
    public Material material(@NotNull Material fallback) {
        return material != null ? material : fallback;
    }

    public int customModelData(int fallback) {
        return customModelData != null ? customModelData : fallback;
    }

    @Nullable
    public String customModelDataString(@Nullable String fallback) {
        return customModelDataString != null ? customModelDataString : fallback;
    }

    public static ItemOptions index(int index) {
        return new ItemOptions(index, null, null, null);
    }

    public static ItemOptions material(int index, @Nullable Material material) {
        return new ItemOptions(index, material, null, null);
    }

    public static ItemOptions model(int index, @Nullable Material material, int customModelDataInt) {
        return new ItemOptions(index, material, customModelDataInt, null);
    }

    @Deprecated
    public static ItemOptions item(int index, @Nullable ItemStack from) {

        Material mat = from != null ? from.getType() : null;
        Integer customModelData = null;
        if (from != null && from.hasItemMeta() && from.getItemMeta().hasCustomModelData()) {
            customModelData = from.getItemMeta().getCustomModelData();
        }

        return new ItemOptions(index, mat, customModelData, null);
    }
}
