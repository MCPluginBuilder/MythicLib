package io.lumine.mythic.lib.gui.builtin;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.ChatInput;
import io.lumine.mythic.lib.util.ItemBuilder;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.gui.PluginInventory;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.version.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class AttributeCreator extends PluginInventory {
    private final AttributeExplorer explorer;
    private final boolean legacy;

    private NamespacedKey namespacedKey;
    private double amount;
    private AttributeModifier.Operation operation;
    private EquipmentSlotGroup group;
    @BackwardsCompatibility(version = "1.21")
    private UUID legacyUuid;
    @BackwardsCompatibility(version = "1.21")
    private String legacyName;
    @BackwardsCompatibility(version = "1.21")
    private EquipmentSlot legacySlot;

    public AttributeCreator(AttributeExplorer inv) {
        super(inv.getPlayer());
        explorer = inv;
        legacy = inv.isLegacy();
    }

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 36, "Attribute Creation..");

        if (!legacy) {
            inv.setItem(11, new ItemBuilder(Material.OAK_SIGN, "&6Name-Spaced Key").setLore("", "&7Current Value: &6" + (namespacedKey == null ? "None" : namespacedKey)));
        } else {
            inv.setItem(10, new ItemBuilder(Material.COMMAND_BLOCK_MINECART, "&6UUID").setLore("", "&7Current Value: &6" + (legacyUuid == null ? "None" : legacyUuid)));
            inv.setItem(11, new ItemBuilder(Material.OAK_SIGN, "&6Name").setLore("", "&7Current Value: &6" + (legacyName == null ? "None" : legacyName)));
        }
        inv.setItem(12, new ItemBuilder(Material.CHEST, "&6Amount").setLore("", "&7Current Value: &6" + amount));
        inv.setItem(13, new ItemBuilder(Material.REPEATER, "&6Operation").setLore("", "&7Current Value: &6" + (operation == null ? "None" : operation.name())));

        if (!legacy) {
            inv.setItem(14, new ItemBuilder(Material.LEATHER_HELMET, "&6Slot Group").setLore("", "&7Current Value: &6" + (group == null ? "None" : group.toString())));
        } else {
            inv.setItem(14, new ItemBuilder(Material.LEATHER_HELMET, "&6Slot").setLore("", "&7Current Value: &6" + (legacySlot == null ? "None" : legacySlot.name())));
        }

        inv.setItem(30, new ItemBuilder(Material.BARRIER, "&6" + AltChar.rightArrow + " Back"));
        inv.setItem(32, new ItemBuilder(Material.GREEN_TERRACOTTA, "&aAdd Attribute!"));

        return inv;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!event.getInventory().equals(event.getClickedInventory()))
            return;

        ItemStack item = event.getCurrentItem();
        if (!UtilityMethods.isMetaItem(item))
            return;

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + AltChar.rightArrow + " Back"))
            explorer.open();

        else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Add Attribute!")) {

            if (amount == 0) {
                getPlayer().sendMessage(ChatColor.RED + "> You must specify a valid amount!");
                return;
            }

            final AttributeModifier.Operation operation = this.operation == null ? AttributeModifier.Operation.ADD_NUMBER : this.operation;
            final AttributeModifier modifier;
            if (!legacy) {
                final NamespacedKey key = this.namespacedKey == null ? new NamespacedKey(MythicLib.plugin, UUID.randomUUID().toString()) : this.namespacedKey;
                //final EquipmentSlotGroup group = this.group == null ? EquipmentSlotGroup.ANY : group;
                modifier = VersionUtils.attrMod(key, amount, operation);
            } else {
                final UUID uuid = this.legacyUuid == null ? UUID.randomUUID() : legacyUuid;
                final String name = this.legacyName == null ? "" : legacyName;
                modifier = new AttributeModifier(uuid, name, amount, operation);
            }

            explorer.getTarget().getAttribute(explorer.getExplored()).addModifier(modifier);
            explorer.open();
            getPlayer().sendMessage(ChatColor.YELLOW + "> Attribute successfully added.");
        }

        // NSK
        else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Name-Spaced Key")) {
            input("name-spaced key (eg `myplugin:iron_skin_pot`)", input -> {
                this.namespacedKey = Objects.requireNonNull(NamespacedKey.fromString(input, MythicLib.plugin));
                return namespacedKey.toString();
            });
        }

        // Slot group
        else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Slot Group")) {
            for (EquipmentSlot slot : EquipmentSlot.values())
                getPlayer().sendMessage(ChatColor.YELLOW + "- " + slot.name().toLowerCase());
            input("slot group", input -> {
                group = EquipmentSlotGroup.getByName(UtilityMethods.enumName(input));
                return group.toString();
            });
        }

        // Modifier operation
        else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Operation")) {
            for (AttributeModifier.Operation operation : AttributeModifier.Operation.values())
                getPlayer().sendMessage(ChatColor.YELLOW + "- " + operation.name().toLowerCase());
            input("operation", input -> {
                operation = AttributeModifier.Operation.valueOf(UtilityMethods.enumName(input));
                return operation.name();
            });
        }

        // Amount
        else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Amount")) {
            input("amount", input -> {
                this.amount = Double.parseDouble(input);
                return AttributeExplorer.FORMAT.format(this.amount);
            });
        }

        // Legacy slot
        else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Slot")) {
            for (EquipmentSlot slot : EquipmentSlot.values())
                getPlayer().sendMessage(ChatColor.YELLOW + "- " + slot.name());
            input("slot", input -> {
                this.legacySlot = EquipmentSlot.valueOf(UtilityMethods.enumName(input));
                return legacySlot.name();
            });
        }

        // Legacy UUID
        else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "UUID")) {
            input("UUID", input -> {
                this.legacyUuid = UUID.fromString(input);
                return legacyUuid.toString();
            });
        }

        // Legacy slot
        else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Name"))
            input("name", input -> this.legacyName = input);
    }

    private void input(@NotNull String inputChatMessageObject,
                       @NotNull Function<String, String> inputHandler) {
        getPlayer().closeInventory();
        getPlayer().sendMessage(ChatColor.YELLOW + "Write in the chat the desired " + inputChatMessageObject + ".");
        new ChatInput(getPlayer(), input -> {
            try {
                final String stringResult = inputHandler.apply(input);
                getPlayer().sendMessage(ChatColor.YELLOW + "Value set to " + ChatColor.GOLD + stringResult + ChatColor.YELLOW + ".");
                open();
                return true;

            } catch (Exception exception) {
                getPlayer().sendMessage(ChatColor.RED + "'" + input + "' is not a valid input. Type 'cancel' to cancel.");
                return false;
            }
        }, this::open);
    }
}