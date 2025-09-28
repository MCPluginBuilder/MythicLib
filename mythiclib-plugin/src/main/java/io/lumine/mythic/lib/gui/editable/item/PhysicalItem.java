package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.gui.util.IconOptions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PhysicalItem<T extends GeneratedInventory> extends InventoryItem<T> {
    private final String id;
    private final IconOptions iconOptions;
    private final String name;
    private final List<String> lore;

    public PhysicalItem(@NotNull ConfigurationSection config) {
        this(null, config);
    }

    public PhysicalItem(@Nullable InventoryItem<T> parent, @NotNull ConfigurationSection config) {
        super(parent, config);

        this.id = config.getName();
        this.iconOptions = IconOptions.from(config);
        this.name = config.getString("name");
        this.lore = config.getStringList("lore");
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void preprocessMeta(@NotNull T inv, int index, @NotNull ItemMeta meta) {
        // Nothing
    }

    /**
     * Preprocesses item lore before PAPI placeholders, coloring
     * are applied. Made to be overrided by subclasses.
     */
    public void preprocessLore(@NotNull T inv, int index, @NotNull List<String> lore) {
        // Nothing
    }

    /**
     * Preprocesses item name before applying PAPI placeholders and coloring.
     * Made to be overrided by subclasses.
     */
    public String preprocessName(@NotNull T inv, int index, @NotNull String name) {
        // Nothing
        return name;
    }

    @Nullable
    public ItemStack getDisplayedItem(@NotNull T inv, int n) {
        return getDisplayedItem(inv, ItemOptions.index(n));
    }

    /**
     * @param inv     Generated inventory being opened by a player
     * @param options Options when generating the item
     * @return Item that will be displayed in the generated inventory
     */
    @Nullable
    public ItemStack getDisplayedItem(T inv, ItemOptions options) {
        Placeholders placeholders = getPlaceholders(inv, options.index());
        OfflinePlayer effectivePlayer = getEffectivePlayer(inv, options.index());
        IconOptions iconOptions = options.icon().combine(this.iconOptions);
        ItemStack item = new ItemStack(iconOptions.getMaterialElse(Material.BARRIER));

        // Meta can sometimes be null (when material is AIR)
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {

            // Preprocess item meta
            preprocessMeta(inv, options.index(), meta);

            // Display name
            if (name != null) {
                String rawName = preprocessName(inv, options.index(), name); // Preprocess
                rawName = placeholders.apply(effectivePlayer, rawName); // Apply placeholders (+ color codes)
                meta.setDisplayName(rawName); // Set
            }

            // Apply icon options (custom model data, item model...)
            iconOptions.applyToItemMeta(meta);

            // Lore
            if (this.lore != null && !this.lore.isEmpty()) {
                List<String> lore = new ArrayList<>(this.lore); // Clone
                preprocessLore(inv, options.index(), lore); // Preprocess

                List<String> workLore = new ArrayList<>();
                for (String line : lore) {
                    // Splitting the lines allows for internal placeholders to add line breaks
                    var parsed = placeholders.apply(effectivePlayer, line).split("\n");
                    for (String str : parsed) workLore.add(ChatColor.GRAY + str);
                }

                meta.setLore(workLore); // Set
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    @NotNull
    public abstract Placeholders getPlaceholders(T inv, int n);
}
