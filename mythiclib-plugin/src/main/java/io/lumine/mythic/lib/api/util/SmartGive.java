package io.lumine.mythic.lib.api.util;


import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Deprecated
public class SmartGive {
    private final io.lumine.mythic.lib.util.SmartGive delegate;

    @Deprecated
    public SmartGive(Player player) {
        delegate = new io.lumine.mythic.lib.util.SmartGive(player);
    }

    @Deprecated
    public void give(ItemStack... item) {
        delegate.give(item);
    }

    @Deprecated
    public void give(List<ItemStack> item) {
        delegate.give(item);
    }
}
