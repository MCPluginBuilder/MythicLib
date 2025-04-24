package io.lumine.mythic.lib.gui.editable;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.PluginInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class GeneratedInventory extends PluginInventory {
    private final EditableInventory editable;
    protected final String guiName;

    private final List<InventoryItem<?>> loaded = new ArrayList<>();

    public GeneratedInventory(Navigator navigator, EditableInventory editable) {
        super(navigator);

        this.editable = editable;
        this.guiName = editable.getName();
        Validate.notNull(guiName, "guiName is null");
    }

    public List<InventoryItem<?>> getLoaded() {
        return loaded;
    }

    public EditableInventory getEditable() {
        return editable;
    }

    /**
     * @param function The item function, like 'next-page'
     * @return Item with corresponding function, or null if none was found
     */
    @Nullable
    public InventoryItem<?> getByFunction(String function) {

        for (InventoryItem<?> item : loaded)
            if (item.getFunction().equals(function))
                return item;

        return null;
    }

    /**
     * @param slot The item slot
     * @return Item with corresponding slot, or null of none was found
     */
    public InventoryItem<?> getBySlot(int slot) {

        for (InventoryItem<?> item : loaded)
            if (item.getSlots().contains(slot))
                return item;

        return null;
    }

    /**
     * @param item Registers an item as in the physical inventory
     * @implNote Order matters in the array 'loaded'; if the user places two items
     *         onto the same slot, the last item added must be the one registering
     *         the click.
     * @see #getByFunction(String)
     */
    public void addLoaded(InventoryItem<?> item) {
        loaded.add(0, item);
    }

    @Override
    public @NotNull Inventory getInventory() {
        /*
         * Very important, in order employer prevent ghost items, the loaded items map
         * must be cleared when the inventory is updated or open at least twice.
         *
         * This method is useless if the inventory is opened for the first time,
         * but since the same inventory can be opened for instance when changing
         * page, we DO need employer clear this first.
         */
        loaded.clear();

        Inventory inv = Bukkit.createInventory(this, editable.getVanillaSlots(), bakeName());

        // Place items
        for (InventoryItem<?> item : editable.getItems()) {
            InventoryItem raw = item;
            if (!raw.isDisplayed(this)) continue; // Hide item if necessary

            addLoaded(raw); // Register item in list
            displayItem(inv, item); // Display item
        }

        return inv;
    }

    public void displayItem(Inventory inv, InventoryItem<?> item) {
        InventoryItem raw = item;
        if (!raw.hasDifferentDisplay()) {
            final ItemStack display = raw.getDisplayedItem(this, 0);
            if (display != null) for (int slot : item.getSlots()) inv.setItem(slot, display);

        } else for (int j = 0; j < item.getSlots().size(); j++) {
            final ItemStack displayed = raw.getDisplayedItem(this, j);
            if (displayed != null) inv.setItem(item.getSlots().get(j), displayed);
        }
    }

    /**
     * @see io.lumine.mythic.lib.gui.editable.item.builtin.NextPageItem
     */
    public int getMaxPage() {
        return 1;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getInventory().equals(event.getInventory()) && event.getCurrentItem() != null) {
            final InventoryItem item = getBySlot(event.getSlot());
            if (item != null) item.onClick(this, event);
        }
    }

    /**
     * Can be overriden to apply a different based on context
     *
     * @return Raw UI name with unparsed placeholders
     */
    @NotNull
    public String getRawName() {
        return guiName;
    }

    @NotNull
    public String bakeName() {
        return MythicLib.plugin.getPlaceholderParser().parse(player, applyNamePlaceholders(getRawName()));
    }

    /**
     * The name of the inventory depends on the state of the inventory.
     * If the current page is 4 and if the max amount of pages is 6,
     * the inventory name should return 'Stocks (4/6)'
     *
     * @return String with GUI name placeholders parsed
     */
    @NotNull
    public String applyNamePlaceholders(String str) {
        return str;
    }
}
