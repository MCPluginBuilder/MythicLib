package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class ItemMultiplex<T extends GeneratedInventory> extends InventoryItem<T> {
    public ItemMultiplex(ConfigurationSection config) {
        super(config);
    }

    @Override
    public boolean hasDifferentDisplay() {
        return false;
    }

    @Override
    public ItemStack getDisplayedItem(@NotNull T inv, int n) {
        throw new RuntimeException("TODO");
    }

    public abstract InventoryItem<T> multiplex(@NotNull T generated, int n);

    @Override
    public boolean isDisplayed(@NotNull T generated) {
        throw new RuntimeException("TODO");
    }
}
