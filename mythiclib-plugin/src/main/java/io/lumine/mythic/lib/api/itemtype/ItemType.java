package io.lumine.mythic.lib.api.itemtype;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface ItemType {

    /**
     * Returns true if the given ItemStack is part of this type.
     */
    boolean matches(ItemStack stack);

    /**
     * Returns a readable string defining the item type.
     * This is mainly used for error logging
     */
    String display();

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    public static ItemType fromString(String input) {

        if (input.contains(".") || input.contains("%") || input.contains("?")) {
            String[] split = input.split("[.%?]");
            Validate.isTrue(split.length == 2, "Please specify a type and ID");
            return new MMOItemType(split[0], split[1]);

        } else return new VanillaType(Material.valueOf(UtilityMethods.enumName(input)));
    }

    public static ItemType fromItemStack(ItemStack item) {
        NBTItem nbtItem = NBTItem.get(item);
        if (nbtItem.hasTag("MMOITEMS_ITEM_TYPE"))
            return new MMOItemType(nbtItem.getString("MMOITEMS_ITEM_TYPE"), nbtItem.getString("MMOITEMS_ITEM_ID"));
        return new VanillaType(item.getType());
    }
}
