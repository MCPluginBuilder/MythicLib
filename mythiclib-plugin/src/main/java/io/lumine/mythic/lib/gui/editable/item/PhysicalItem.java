package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PhysicalItem<T extends GeneratedInventory> extends InventoryItem<T> {
    private final String id, texture;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int customModelDataInt;
    private final String customModelDataString;
    private final NamespacedKey itemModel;
    private final boolean hideFlags, hideTooltip;

    public PhysicalItem(@NotNull ConfigurationSection config) {
        this(null, config);
    }

    public PhysicalItem(@Nullable InventoryItem<T> parent, @NotNull ConfigurationSection config) {
        super(parent, config);

        this.id = config.getName();
        this.material = config.getString("item") != null ? Material.valueOf(UtilityMethods.enumName(config.getString("item"))) : Material.DIRT;
        this.name = config.getString("name");
        this.lore = config.getStringList("lore");
        this.hideFlags = config.getBoolean("hide-flags");
        this.hideTooltip = config.getBoolean("hide-tooltip");
        this.customModelDataInt = config.getInt("custom-model-data");
        this.customModelDataString = config.contains("custom-model-data-string") ? config.getString("custom-model-data-string") : null;
        this.itemModel = config.contains("item-model") ? NamespacedKey.fromString(config.getString("item-model")) : null;
        this.texture = config.getString("texture");
    }

    @NotNull
    public String getId() {
        return id;
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

    public void preprocessItem(@NotNull T inv, int index, @NotNull ItemStack item) {
        // Nothing
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
        ItemStack item = new ItemStack(options.material(material));

        // Meta can sometimes be null with AIR for instance)
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {

            // Display name
            if (name != null) {
                String rawName = preprocessName(inv, options.index(), name); // Preprocess
                rawName = placeholders.apply(effectivePlayer, rawName); // Apply placeholders
                rawName = MythicLib.plugin.parseColors(rawName); // Color codes
                meta.setDisplayName(rawName); // Set
            }

            // Hide flags
            if (hideFlags) meta.addItemFlags(ItemFlag.values());
            if (hideTooltip) meta.setHideTooltip(true);

            // Lore
            if (this.lore != null && !this.lore.isEmpty()) {
                List<String> lore = new ArrayList<>(this.lore); // Clone
                preprocessLore(inv, options.index(), lore); // Preprocess

                List<String> workLore = new ArrayList<>();
                for (String line : lore) {
                    // Splitting the lines allows for internal placeholders to add line breaks
                    String[] parsed = MythicLib.plugin.parseColors(placeholders.apply(effectivePlayer, line)).split("\n");
                    for (String str : parsed) workLore.add(ChatColor.GRAY + str);
                }

                meta.setLore(workLore); // Set
            }

            // Custom model data integer
            int customModelDataInt = options.customModelData(this.customModelDataInt);
            if (customModelDataInt != 0) meta.setCustomModelData(customModelDataInt);

            // Custom model data string
            String customModelDataComponent = options.customModelDataString(customModelDataString);
            if (customModelDataComponent != null) {
                CustomModelDataComponent comp = meta.getCustomModelDataComponent();
                comp.setStrings(Collections.singletonList(customModelDataComponent));
                meta.setCustomModelDataComponent(comp);
            }

            // Item model
            if (itemModel != null) meta.setItemModel(itemModel);

            // Skull texture
            if (texture != null && meta instanceof SkullMeta) UtilityMethods.setTextureValue((SkullMeta) meta, texture);

            item.setItemMeta(meta);
        }

        return item;
    }

    @NotNull
    public abstract Placeholders getPlaceholders(T inv, int n);
}
