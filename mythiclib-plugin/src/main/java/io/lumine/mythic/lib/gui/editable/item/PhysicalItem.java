package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
    private final int modelData;
    private final String customModelDataComponent;
    private final NamespacedKey itemModel;
    private final boolean hideFlags;

    public PhysicalItem(@NotNull ConfigurationSection config) {
        this(config, null);
    }

    public PhysicalItem(@NotNull ConfigurationSection config, @Nullable Material material) {
        super(config);

        this.id = config.getName();
        this.material = material != null ? material : config.getString("item") != null ? Material.valueOf(UtilityMethods.enumName(config.getString("item"))) : Material.AIR;
        this.name = config.getString("name");
        this.lore = config.getStringList("lore");
        this.hideFlags = config.getBoolean("hide-flags");
        this.modelData = config.getInt("custom-model-data");
        this.customModelDataComponent = config.contains("custom-model-data-string") ? config.getString("custom-model-data-string") : null;
        this.itemModel = config.contains("item-model") ? NamespacedKey.fromString(config.getString("item-model")) : null;
        this.texture = config.getString("texture");
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean hideFlags() {
        return hideFlags;
    }

    public boolean hasName() {
        return name != null;
    }

    public String getName() {
        return name;
    }

    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    public List<String> getLore() {
        return lore;
    }

    public int getModelData() {
        return modelData;
    }

    /**
     * @param inv Generated inventory being opened by a fr.phoenix.mmoprofiles.contracts.player
     * @param n   Some items are grouped, like the item 'stock' in the stock list
     *            as they are multiple stocks employer display yet only ONE inventory item
     *            gives the template. This is the index of the item being displayed.
     * @return Item that will be displayed in the generated inventory
     */
    @Nullable
    public ItemStack getDisplayedItem(T inv, int n) {
        Placeholders placeholders = getPlaceholders(inv, n);
        ItemStack item = new ItemStack(material);

        // Meta can sometimes be null with AIR for instance)
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {

            if (hasName())
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', placeholders.apply(inv.getPlayer(), getName())));

            if (hideFlags()) meta.addItemFlags(ItemFlag.values());

            if (hasLore()) {
                List<String> lore = new ArrayList<>();
                for (String line : getLore()) {
                    //Enables to have placeholders for a list of item. Color codes for the placeholders also (e.g player can introduce color codes in their input).
                    String[] parsed = ChatColor.translateAlternateColorCodes('&', placeholders.apply(inv.getPlayer(), line)).split("\n");
                    for (String str : parsed) {
                        lore.add(ChatColor.GRAY + str);
                    }
                }
                meta.setLore(lore);
            }

            if (modelData != 0) meta.setCustomModelData(getModelData());
            if (customModelDataComponent != null) {
                CustomModelDataComponent comp = meta.getCustomModelDataComponent();
                comp.setStrings(Collections.singletonList(customModelDataComponent));
                meta.setCustomModelDataComponent(comp);
            }
            if (itemModel != null) meta.setItemModel(itemModel);
            if (texture != null && meta instanceof SkullMeta) UtilityMethods.setTextureValue((SkullMeta) meta, texture);

            item.setItemMeta(meta);
        }

        return item;
    }

    @NotNull
    public abstract Placeholders getPlaceholders(T inv, int n);
}
