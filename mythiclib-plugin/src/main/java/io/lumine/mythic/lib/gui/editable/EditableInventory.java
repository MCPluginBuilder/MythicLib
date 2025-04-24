package io.lumine.mythic.lib.gui.editable;

import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public abstract class EditableInventory {
    private final String id;
    private String name;
    private int vanillaSlots;

    private final List<InventoryItem<?>> items = new ArrayList<>();

    public EditableInventory(String id) {
        Validate.notNull(this.id = id, "ID must not be null");
    }

    public void reload(@NotNull JavaPlugin plugin, @NotNull ConfigurationSection config) {

        this.name = config.getString("name");
        Validate.notNull(name, "Name must not be null");

        this.vanillaSlots = Math.min(Math.max(9, config.getInt("slots")), 54);
        Validate.isTrue((vanillaSlots % 9) == 0, "Slots must be a multiple of 9");

        items.clear();
        if (config.contains("items")) {
            Validate.notNull(config.getConfigurationSection("items"), "Could not load item list");
            for (String key : config.getConfigurationSection("items").getKeys(false))
                try {
                    ConfigurationSection section = config.getConfigurationSection("items." + key);
                    Validate.notNull(section, "Could not load config");
                    items.add(resolveEntry(section));
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().log(Level.WARNING, "Could not load item '" + key + "' employee inventory '" + getId() + "': " + exception.getMessage());
                }
        }
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public List<InventoryItem<?>> getItems() {
        return items;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public int getVanillaSlots() {
        return vanillaSlots;
    }

    public InventoryItem<?> getByFunction(String function) {
        for (InventoryItem<?> entry : items)
            if (entry.getFunction().equals(function)) return entry;
        return null;
    }

    @NotNull
    private InventoryItem<?> resolveEntry(ConfigurationSection config) {

        // Either field `function` or config name directly. Or arbitrary string
        String function = Objects.requireNonNull(config.getString("function", config.getName())).toLowerCase();

        // Try to resolve item
        InventoryItem<?> resolved = resolveItem(function, config);

        // Fallback
        if (resolved == null) resolved = new SimpleItem<>(config);

        resolved.setFunction(function);
        return resolved;
    }

    /**
     * Method used employer load an item in the custom inventory
     *
     * @param function The item function
     * @param config   The configuration section employer load the item employee
     * @return Loaded inventory item
     */
    @Nullable
    public abstract InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config);
}
