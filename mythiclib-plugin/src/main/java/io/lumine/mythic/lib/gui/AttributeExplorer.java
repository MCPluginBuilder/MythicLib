package io.lumine.mythic.lib.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.explorer.ChatInput;
import io.lumine.mythic.lib.api.explorer.ItemBuilder;
import io.lumine.mythic.lib.api.stat.handler.AttributeStatHandler;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.util.ReflectionUtils;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AttributeExplorer extends PluginInventory {
    private final Player target;
    private final boolean legacy;

    /**
     * Explored attribute
     */
    private Attribute explored;
    private List<AttributeModifier> modifiers;
    private int modifierOffset, attributeOffset;

    private static final int[]
            ATTRIBUTE_SLOTS = {37, 38, 39, 40, 41, 42, 43, 46, 47, 48, 49, 50, 51, 52},
            MODIFIER_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    private final List<AttributeStatHandler> attributes;

    public static final DecimalFormat FORMAT = new DecimalFormat("0.#####");

    public AttributeExplorer(Player player, Player target) {
        super(player);

        Validate.notNull(target, "Target cannot be null");
        this.target = target;

        // Read attributes
        legacy = MythicLib.plugin.getVersion().isUnder(1, 21);
        attributes = MythicLib.plugin.getStats().getHandlers().stream()
                .filter(handler -> handler instanceof AttributeStatHandler)
                .map(handler -> (AttributeStatHandler) handler)
                .collect(Collectors.toList());
    }

    public Attribute getExplored() {
        return explored;
    }

    public Player getTarget() {
        return target;
    }

    public boolean isLegacy() {
        return legacy;
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 54, "Attributes of " + target.getName() + (explored == null ? "" : " (" + getName(explored) + ")"));

        inv.setItem(4, new ItemBuilder(Material.WHITE_BED, "&6Refresh &8(Click)"));

        int j = 0;
        while (j < Math.min(ATTRIBUTE_SLOTS.length, attributes.size() - attributeOffset)) {

            final AttributeStatHandler handler = attributes.get(attributeOffset + j);
            final AttributeInstance ins = target.getAttribute(handler.getAttribute());
            if (ins == null) continue;

            ItemStack item = new ItemStack(handler.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + getName(handler.getAttribute()));
            meta.addItemFlags(ItemFlag.values());
            if (MythicLib.plugin.getVersion().isAbove(1, 20, 5)) VersionUtils.addEmptyAttributeModifier(meta);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + handler.getDescription());
            lore.add("");
            lore.add(ChatColor.GRAY + "Total Value: " + ChatColor.GOLD + ChatColor.BOLD + FORMAT.format(ins.getValue()));
            lore.add(ChatColor.GRAY + AltChar.smallListDash + " Base Value: " + ChatColor.GOLD + FORMAT.format(ins.getBaseValue()));
            lore.add(ChatColor.GRAY + AltChar.smallListDash + " Due to Modifiers: " + ChatColor.GOLD + FORMAT.format(ins.getValue() - ins.getBaseValue()));
            lore.add(ChatColor.GRAY + AltChar.smallListDash + " Default Value: " + ChatColor.GOLD + FORMAT.format(ins.getDefaultValue()));
            lore.add("");
            lore.add(ChatColor.GRAY + "Modifier Count: " + ChatColor.GOLD + ins.getModifiers().size());
            lore.add("");
            lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Left click to explore.");
            lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Right click to set the base value.");
            lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Shift click to reset base value.");

            meta.getPersistentDataContainer().set(ATTRIBUTE_KEY, PersistentDataType.STRING, Attributes.name(handler.getAttribute()));
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(ATTRIBUTE_SLOTS[j++], item);
        }

        ItemStack fillAttribute = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, "&cNo Attribute");
        while (j < ATTRIBUTE_SLOTS.length) inv.setItem(ATTRIBUTE_SLOTS[j++], fillAttribute);

        if (attributeOffset + ATTRIBUTE_SLOTS.length < attributes.size())
            inv.setItem(53, new ItemBuilder(Material.ARROW, "&6Next Attributes"));

        if (attributeOffset > 0)
            inv.setItem(45, new ItemBuilder(Material.ARROW, "&6Previous Attributes"));

        if (explored != null) {

            inv.setItem(1, new ItemBuilder(Material.WRITABLE_BOOK, "&6New Modifier.."));
            inv.setItem(7, new ItemBuilder(Material.BARRIER, "&6" + AltChar.rightArrow + " Back"));

            j = 0;
            while (j < Math.min(MODIFIER_SLOTS.length, modifiers.size() - modifierOffset)) {
                AttributeModifier modifier = modifiers.get(modifierOffset + j);

                ItemStack item = new ItemStack(Material.GRAY_DYE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "Modifier n" + (j + 1));

                List<String> lore = new ArrayList<>();
                lore.add("");
                if (!legacy) {
                    lore.add(ChatColor.GRAY + "Key: " + ChatColor.GOLD + modifier.getKey());
                } else {
                    lore.add(ChatColor.GRAY + "UUID: " + ChatColor.GOLD + ReflectionUtils.invoke(AttributeModifier_getUniqueId, modifier));
                    lore.add(ChatColor.GRAY + "Name: " + ChatColor.GOLD + modifier.getName());
                }
                lore.add(ChatColor.GRAY + "Amount: " + ChatColor.GOLD + modifier.getAmount());
                lore.add(ChatColor.GRAY + "Operation: " + ChatColor.GOLD + modifier.getOperation());
                if (!legacy) {
                    lore.add(ChatColor.GRAY + "Slot Group: " + ChatColor.GOLD + modifier.getSlotGroup());
                } else {
                    lore.add(ChatColor.GRAY + "Slot: " + ChatColor.GOLD + modifier.getSlot());
                }
                lore.add("");
                lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Right click to remove.");

                meta.getPersistentDataContainer().set(MODIFIER_KEY, PersistentDataType.STRING, !legacy
                        ? modifier.getKey().toString()
                        : ReflectionUtils.invoke(AttributeModifier_getUniqueId, modifier).toString());
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

    private static final Method AttributeModifier_getUniqueId = ReflectionUtils.getDeclaredMethod(AttributeModifier.class, "getUniqueId");

    public void setExplored(Attribute attribute) {
        explored = attribute;
        modifiers = explored != null ? new ArrayList<>(target.getAttribute(explored).getModifiers()) : null;
    }

    private String getName(Attribute attribute) {
        return UtilityMethods.caseOnWords(Attributes.name(attribute)
                .replace("GENERIC_", "")
                .replace("PLAYER_", "")
                .toLowerCase().replace("_", " "));
    }

    private static final NamespacedKey ATTRIBUTE_KEY = new NamespacedKey(MythicLib.plugin, "attribute");
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey(MythicLib.plugin, "modifier");

    @Override
    public void whenClicked(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!event.getInventory().equals(event.getClickedInventory()))
            return;

        ItemStack item = event.getCurrentItem();
        if (!UtilityMethods.isMetaItem(item))
            return;

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Refresh " + ChatColor.DARK_GRAY + "(Click)")) {
            setExplored(explored);
            open();
            return;
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + AltChar.rightArrow + " Back")) {
            setExplored(null);
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

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Next Attributes")) {
            attributeOffset += ATTRIBUTE_SLOTS.length;
            open();
            return;
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Previous Attributes")) {
            attributeOffset -= ATTRIBUTE_SLOTS.length;
            open();
            return;
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "New Modifier..")) {
            new AttributeCreator(this).open();
            return;
        }

        String tag = item.getItemMeta().getPersistentDataContainer().get(MODIFIER_KEY, PersistentDataType.STRING);
        if (tag != null && event.getAction() == InventoryAction.PICKUP_HALF) {
            final AttributeModifier mod = !legacy
                    ? VersionUtils.getModifier(getPlayer().getAttribute(explored), NamespacedKey.fromString(tag))
                    : getModifier(getPlayer().getAttribute(explored), UUID.fromString(tag));
            Validate.notNull(mod, "Could not find attribute modifier with tag '" + tag + "'");
            target.getAttribute(explored).removeModifier(mod);
            getPlayer().sendMessage(ChatColor.YELLOW + "> Modifier successfully removed.");
            setExplored(explored);
            open();
            return;
        }

        tag = item.getItemMeta().getPersistentDataContainer().get(ATTRIBUTE_KEY, PersistentDataType.STRING);
        if (tag != null) {
            final Attribute attribute = Attributes.fromName(tag);

            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                setExplored(attribute);
                open();
            } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                target.getAttribute(attribute).setBaseValue(target.getAttribute(attribute).getDefaultValue());
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

                    getPlayer().sendMessage(ChatColor.YELLOW + "> Base value set to " + ChatColor.GOLD + FORMAT.format(d) + ChatColor.YELLOW + ".");
                    target.getAttribute(attribute).setBaseValue(d);
                    open();
                    return true;
                });
            }
        }
    }

    /**
     * AttributeInstance#getModifier(UUID) does not exist in 1.19 and lower
     */
    @Nullable
    @SuppressWarnings("removal")
    @BackwardsCompatibility(version = "1.19")
    private static AttributeModifier getModifier(@NotNull AttributeInstance instance, @NotNull UUID uuid) {
        for (AttributeModifier modifier : instance.getModifiers())
            if (modifier.getUniqueId().equals(uuid))
                return modifier;
        return null;
    }
}
