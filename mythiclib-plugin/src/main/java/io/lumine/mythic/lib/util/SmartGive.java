package io.lumine.mythic.lib.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SmartGive {
    private final Inventory inv;
    private final Location loc;

    public SmartGive(Player player) {
        inv = player.getInventory();
        loc = player.getLocation();
    }

    /**
     * Either give directly the item to the player, or drop it on
     * the ground if there is not enough space in the player inventory.
     */
    public void give(ItemStack... items) {
        for (ItemStack drop : inv.addItem(items).values())
            loc.getWorld().dropItem(loc, drop);
    }

    public void give(List<ItemStack> items) {
        for (ItemStack drop : inv.addItem(items.toArray(new ItemStack[0])).values())
            loc.getWorld().dropItem(loc, drop);
    }
}

