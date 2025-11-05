package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class InventoryItem<T extends GeneratedInventory> {
    private final List<Integer> slots = new ArrayList<>();

    private String function;

    protected final DecimalFormat ONE_DIGIT = new DecimalFormat("0.#");

    public InventoryItem(@NotNull ConfigurationSection config) {
        this(null, config);
    }

    public InventoryItem(@Nullable InventoryItem<T> parent, @NotNull ConfigurationSection config) {

        // Non null parent
        if (parent != null) {
            this.slots.addAll(parent.slots);
            function = parent.function;
        }

        // Default setup
        else {
            config.getStringList("slots").forEach(str -> slots.add(Integer.parseInt(str)));
        }
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

    /**
     * @param inv Inventory being generated
     * @param n   Index of item being generated
     * @return Player relative to which placeholders are computed when the item is
     *         being displayed in the inventory. Most of the time, it's just
     *         the player opening the inventory, but for friends or party members,
     *         being able to parse placeholders based on other players is great too.
     */
    @NotNull
    public OfflinePlayer getEffectivePlayer(T inv, int n) {
        return inv.getPlayer();
    }

    public void onClick(@NotNull T generated, @NotNull InventoryClickEvent event) {
        // Nothing by default
    }
}
