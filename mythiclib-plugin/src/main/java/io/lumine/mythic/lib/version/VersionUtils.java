package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VersionUtils {

    @NotNull
    public static AttributeModifier attrMod(@NotNull NamespacedKey key, double amount, @NotNull AttributeModifier.Operation operation) {
        return VersionWrapper.get().newAttributeModifier(key, amount, operation);
    }

    private static final NamespacedKey NSK_TRICK = new NamespacedKey(MythicLib.plugin, "attr_mod_decoy");
    private static final Attribute NSK_ATTRIBUTE = Attributes.FOLLOW_RANGE;

    @NotNull
    public static AttributeModifier emptyAttributeModifier() {
        return VersionWrapper.get().newAttributeModifier(NSK_TRICK, 0, AttributeModifier.Operation.ADD_NUMBER);
    }

    @Nullable
    public static AttributeModifier getModifier(AttributeInstance instance, NamespacedKey key) {
        for (AttributeModifier modifier : instance.getModifiers())
            if (modifier.getKey().equals(key))
                return modifier;
        return null;
    }

    @NotNull
    public static void addEmptyAttributeModifier(@NotNull ItemMeta meta) {
        meta.addAttributeModifier(NSK_ATTRIBUTE, emptyAttributeModifier());
    }

    public static boolean matches(@NotNull AttributeModifier modifier, @NotNull NamespacedKey key) {
        return VersionWrapper.get().matches(modifier, key);
    }

    @NotNull
    public static VInventoryView getView(@NotNull InventoryEvent event) {
        return VersionWrapper.get().getView(event);
    }

    @NotNull
    public static String name(@NotNull Biome biome) {
        return VersionWrapper.get().getBiomeName(biome);
    }

    @NotNull
    public static String name(@NotNull Sound sound) {
        return VersionWrapper.get().getSoundName(sound);
    }

    @NotNull
    public static VInventoryView getOpen(@NotNull Player player) {
        return VersionWrapper.get().getOpenInventory(player);
    }

    @NotNull
    public static InventoryClickEvent invClickEvent(@NotNull VInventoryView view, @NotNull InventoryType.SlotType type, int slot, @NotNull ClickType click, @NotNull InventoryAction action) {
        return VersionWrapper.get().newInventoryClickEvent(view, type, slot, click, action);
    }
}
