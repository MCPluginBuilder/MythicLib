package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ChatInput implements Listener {
    private final Player player;
    private final Function<String, Boolean> inputHandler;
    private final Runnable cancelHandler;

    @Deprecated
    public ChatInput(Player player, Function<String, Boolean> inputHandler) {
        this.player = player;
        this.inputHandler = inputHandler;
        this.cancelHandler = () -> inputHandler.apply(null);
        Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
    }

    public ChatInput(@NotNull Player player,
                     @NotNull Function<String, Boolean> inputHandler,
                     @NotNull Runnable cancelHandler) {
        this.player = player;
        this.inputHandler = inputHandler;
        this.cancelHandler = cancelHandler;
        Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
    }

    public void close() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void a(AsyncPlayerChatEvent event) {
        if (event.getPlayer().equals(player)) {
            event.setCancelled(true);

            // Cancel input
            if (event.getMessage().equalsIgnoreCase("cancel")) {
                cancelHandler.run();
                close();
                return;
            }

            if (inputHandler.apply(event.getMessage())) close();
        }
    }

    @EventHandler
    public void b(InventoryCloseEvent event) {
        if (event.getPlayer().equals(player)) close();
    }

    @EventHandler
    public void c(InventoryOpenEvent event) {
        if (event.getPlayer().equals(player)) close();
    }

    @EventHandler
    public void c(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) close();
    }
}
