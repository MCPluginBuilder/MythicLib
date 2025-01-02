package io.lumine.mythic.lib.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class PluginInventory implements InventoryHolder {
    private final Player player;

    public PluginInventory(Player player) {
        Validate.notNull(player, "Player cannot be null");
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public abstract Inventory getInventory();

    public abstract void whenClicked(InventoryClickEvent event);

    public void open() {
        if (Bukkit.isPrimaryThread())
            player.openInventory(getInventory());
        else
            Bukkit.getScheduler().runTask(MythicLib.plugin, () -> player.openInventory(getInventory()));
    }
}
