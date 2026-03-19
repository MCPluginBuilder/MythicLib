package io.lumine.mythic.lib.util.input;

import io.lumine.mythic.lib.util.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Allows to collect user input using a chat message.
 * Works reliably on any version without using NMS code.
 */
public class ChatPlayerInput extends PlayerInput {
    public ChatPlayerInput(Plugin plugin, Player player, Runnable onCancel, Predicate<String> onInput) {
        super(plugin, player, onCancel, onInput);
    }

    @Override
    protected void onInit() {
        // Listeners already opened
    }

    @Override
    protected void onClosed() {
        // Listeners already closed
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChatReceive(AsyncPlayerChatEvent event) {
        if (event.getPlayer().equals(this.player)) {
            event.setCancelled(true);
            this.handlePlayerInput(event.getMessage());
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        // Cancel stat edition when opening any GUI
        if (event.getPlayer().equals(getPlayer())) close();
    }
}
