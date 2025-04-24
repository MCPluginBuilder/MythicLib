package io.lumine.mythic.lib.util.io;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.inventory.ItemStack;

public class SafeItemSerializer {
    public static ItemStack nbtDeserialize(String nbt) {
        return NBT.itemStackFromNBT(NBT.parseNBT(nbt));
    }

    public static String nbtSerialize(ItemStack item) {
        return NBT.itemStackToNBT(item).toString();
    }
}
