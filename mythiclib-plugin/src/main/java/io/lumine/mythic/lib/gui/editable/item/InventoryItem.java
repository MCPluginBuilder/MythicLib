package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class InventoryItem<T extends GeneratedInventory> {
    private final List<Integer> slots = new ArrayList<>();

    private String function;

    public InventoryItem(ConfigurationSection config) {
        config.getStringList("slots").forEach(str -> slots.add(Integer.parseInt(str)));
    }

    public void setFunction(@NotNull String function) {
        Validate.isTrue(this.function == null, "Function already provided");

        this.function = function;
    }

    public String getFunction() {
        return function;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public boolean hasDifferentDisplay() {
        return false;
    }

    public boolean isDisplayed(@NotNull T generated) {
        return true;
    }

    public abstract ItemStack getDisplayedItem(@NotNull T inv, int n);

    public void onClick(@NotNull T generated, @NotNull InventoryClickEvent event) {

    }
}
