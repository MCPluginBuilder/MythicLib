package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.gui.util.IconOptions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemOptions {
    private final int index;

    @Nullable
    private final IconOptions icon;

    public ItemOptions(int index, @Nullable IconOptions icon) {
        this.index = index;
        this.icon = icon;
    }

    public int index() {
        return index;
    }

    @NotNull
    public IconOptions icon() {
        return icon == null ? IconOptions.EMPTY : icon;
    }

    //region Static methods

    public static ItemOptions index(int index) {
        return new ItemOptions(index, null);
    }

    public static ItemOptions material(int index, @Nullable Material material) {
        return new ItemOptions(index, new IconOptions(material));
    }

    public static ItemOptions model(int index, @Nullable Material material, int customModelDataInt) {
        return new ItemOptions(index, new IconOptions(material, customModelDataInt));
    }

    @Deprecated
    public static ItemOptions item(int index, @Nullable ItemStack from) {
        return new ItemOptions(index, IconOptions.from(from));
    }

    //endregion
}
