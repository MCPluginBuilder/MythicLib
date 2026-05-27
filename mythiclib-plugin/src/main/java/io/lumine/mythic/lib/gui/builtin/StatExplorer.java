package io.lumine.mythic.lib.gui.builtin;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.handler.AttributeStatHandler;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.gui.PluginInventory;
import io.lumine.mythic.lib.util.ItemBuilder;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StatExplorer extends PluginInventory {
    private final MMOPlayerData targetData;
    private final List<StatHandler> stats;

    private int statOffset = 0, modifierOffset;
    private String explored;

    private static final int[]
            STAT_SLOTS = {37, 38, 39, 40, 41, 42, 43, 46, 47, 48, 49, 50, 51, 52},
            MODIFIER_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#####");
    private static final int MAX_DISPLAY_STATS = 20;

    public StatExplorer(Player player, MMOPlayerData targetData) {
        super(player);

        this.targetData = Objects.requireNonNull(targetData, "Target cannot be null");
        this.stats = new ArrayList<>(MythicLib.plugin.getStats().getHandlers());
    }

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 54, "Stats of " + targetData.getPlayerName());

        inv.setItem(4, new ItemBuilder(Material.WHITE_BED, "&6Refresh &8(Click)"));

        int j = 0;
        while (j < Math.min(STAT_SLOTS.length, stats.size() - statOffset)) {

            final var stat = this.stats.get(statOffset + j);
            final var statInstance = targetData.getStatMap().getInstance(stat.getStat());

            ItemStack item = new ItemStack(stat instanceof AttributeStatHandler ? ((AttributeStatHandler) stat).getMaterial() : Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + UtilityMethods.caseOnWords(stat.getStat().toLowerCase().replace("_", " ")));

            List<String> lore = new ArrayList<>();
            // lore.add(ChatColor.GRAY + handler.getDescription());
            lore.add("");
            lore.add(ChatColor.GRAY + "Total Value: " + ChatColor.GOLD + ChatColor.BOLD + DECIMAL_FORMAT.format(statInstance.getTotal()));
            lore.add(ChatColor.GRAY + AltChar.smallListDash + " Base Value: " + ChatColor.GOLD + DECIMAL_FORMAT.format(statInstance.getBase()));
            lore.add(ChatColor.GRAY + AltChar.smallListDash + " Default Base Value: " + ChatColor.GOLD + DECIMAL_FORMAT.format(statInstance.getDefaultBase()));
            lore.add("");
            lore.add(ChatColor.GRAY + "Modifier Count: " + ChatColor.GOLD + statInstance.getModifiers().size());

            // Lore cannot have more than 256 lines
            // Only display the first N
            var statOverflow = statInstance.getModifiers().size() > MAX_DISPLAY_STATS;
            var modifiers = statOverflow ? new ArrayList<>(statInstance.getModifiers()).subList(0, MAX_DISPLAY_STATS) : statInstance.getModifiers();

            for (var modifier : modifiers) {
                lore.add(ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + modifier.getKey() + ": " + ChatColor.GOLD + DECIMAL_FORMAT.format(modifier.getValue()));
            }
            if (statOverflow) {
                lore.add(ChatColor.DARK_GRAY + "and " + (statInstance.getModifiers().size() - MAX_DISPLAY_STATS) + " more...");
            }

            meta.getPersistentDataContainer().set(STAT_KEY, PersistentDataType.STRING, stat.getStat());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(STAT_SLOTS[j++], item);
        }

        ItemStack fillAttribute = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, "&cNo Attribute");
        while (j < STAT_SLOTS.length) inv.setItem(STAT_SLOTS[j++], fillAttribute);

        if (statOffset + STAT_SLOTS.length < stats.size())
            inv.setItem(53, new ItemBuilder(Material.ARROW, "&6Next Stats"));

        if (statOffset > 0)
            inv.setItem(45, new ItemBuilder(Material.ARROW, "&6Previous Stats"));

        if (explored != null) {

            // Collect player modifiers
            var modifiers = new ArrayList<>(targetData.getStatMap().getInstance(explored).getModifiers());

            inv.setItem(1, new ItemBuilder(Material.WRITABLE_BOOK, "&6New Modifier.."));
            inv.setItem(7, new ItemBuilder(Material.BARRIER, "&6" + AltChar.rightArrow + " Back"));

            j = 0;
            while (j < Math.min(MODIFIER_SLOTS.length, modifiers.size() - modifierOffset)) {
                final var modifier = modifiers.get(modifierOffset + j);

                ItemStack item = new ItemStack(Material.GRAY_DYE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "Modifier n" + (j + 1));

                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GRAY + "Key: " + ChatColor.GOLD + modifier.getKey());
                lore.add(ChatColor.GRAY + "UUID: " + ChatColor.GOLD + modifier.getUniqueId());
                lore.add(ChatColor.GRAY + "Amount: " + ChatColor.GOLD + modifier.getValue());
                lore.add(ChatColor.GRAY + "Type: " + ChatColor.GOLD + modifier.getType());
                lore.add(ChatColor.GRAY + "Slot: " + ChatColor.GOLD + modifier.getSlot());
                lore.add(ChatColor.GRAY + "Source: " + ChatColor.GOLD + modifier.getSource());
                lore.add("");
                lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Right click to remove.");

                meta.getPersistentDataContainer().set(MODIFIER_KEY, PersistentDataType.STRING, modifier.getUniqueId().toString());
                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.setItem(MODIFIER_SLOTS[j++], item);
            }

            ItemStack fillModifier = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, "&cNo Modifier");
            while (j < MODIFIER_SLOTS.length) inv.setItem(MODIFIER_SLOTS[j++], fillModifier);

            if (modifierOffset + MODIFIER_SLOTS.length < modifiers.size())
                inv.setItem(26, new ItemBuilder(Material.ARROW, "&6Next Page"));

            if (modifierOffset > 0)
                inv.setItem(18, new ItemBuilder(Material.ARROW, "&6Previous Page"));
        }

        return inv;
    }

    private static final NamespacedKey STAT_KEY = new NamespacedKey(MythicLib.plugin, "stat");
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey(MythicLib.plugin, "modifier");

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!event.getInventory().equals(event.getClickedInventory()))
            return;

        ItemStack item = event.getCurrentItem();
        if (!UtilityMethods.isMetaItem(item))
            return;

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Refresh " + ChatColor.DARK_GRAY + "(Click)")) {
            open();
            return;
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + AltChar.rightArrow + " Back")) {
            this.explored = null;
            open();
            return;
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Next Page")) {
            modifierOffset += MODIFIER_SLOTS.length;
            open();
            return;
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Previous Page")) {
            modifierOffset -= MODIFIER_SLOTS.length;
            open();
            return;
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Next Stats")) {
            statOffset += STAT_SLOTS.length;
            open();
            return;
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Previous Stats")) {
            statOffset -= STAT_SLOTS.length;
            open();
            return;
        }

        /*
        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "New Modifier..")) {
            new AttributeCreator(this).open();
            return;
        }
         */

        String tag = item.getItemMeta().getPersistentDataContainer().get(MODIFIER_KEY, PersistentDataType.STRING);
        if (tag != null && event.getAction() == InventoryAction.PICKUP_HALF) {
            final var statInstance = targetData.getStatMap().getInstance(this.explored);
            final var mod = statInstance.getModifier(UUID.fromString(tag));
            Validate.notNull(mod, "Could not find attribute modifier with tag '" + tag + "'");
            statInstance.removeModifier(mod.getUniqueId());
            getPlayer().sendMessage(ChatColor.YELLOW + "> Modifier successfully removed.");
            open();
            return;
        }

        tag = item.getItemMeta().getPersistentDataContainer().get(STAT_KEY, PersistentDataType.STRING);
        if (tag != null) {
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                this.explored = tag;
                open();
            }/* else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                final var instance = this.targetData.getStatMap().getInstance(this.explored);
                instance.
                instance.setBaseValue(UtilityMethods.getPlayerDefaultBaseValue(attribute, instance));
                getPlayer().sendMessage(ChatColor.YELLOW + "> Base value of " + ChatColor.GOLD + Attributes.name(attribute) + ChatColor.YELLOW + " successfully reset.");
                open();

            } else if (event.getAction() == InventoryAction.PICKUP_HALF) {

                getPlayer().closeInventory();
                getPlayer().sendMessage(ChatColor.YELLOW + "> Write in the chat the value you want.");
                new ChatInput(getPlayer(), (output) -> {

                    if (output == null) {
                        open();
                        return true;
                    }

                    double d;
                    try {
                        d = Double.parseDouble(output);
                    } catch (NumberFormatException exception) {
                        getPlayer().sendMessage(ChatColor.RED + "> " + output + " is not a valid number. Type 'cancel' to cancel.");
                        return false;
                    }

                    getPlayer().sendMessage(ChatColor.YELLOW + "> Base value set to " + ChatColor.GOLD + DECIMAL_FORMAT.format(d) + ChatColor.YELLOW + ".");
                    target.getAttribute(attribute).setBaseValue(d);
                    open();
                    return true;
                });
            }*/
        }
    }
}
