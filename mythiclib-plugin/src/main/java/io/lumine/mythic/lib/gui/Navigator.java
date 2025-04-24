package io.lumine.mythic.lib.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.Tasks;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Stack;

/**
 * Similar to `NavHost` in Kotlin Jetpack Compose. Keeps track
 * of opened inventories using a stack. Inventories explored
 * by the player are placed on the top of the stack. Any "Back"
 * button will pop topmost inventory and open whatever is below.
 * <p>
 * If properly handled, it avoids the use of Spigot inventory holders
 * which are known to cause performance issues, and permit a few
 * extra features like keeping pagination and other dynamic UI data.
 *
 * @author jules
 */
public class Navigator implements Listener {
    private final Stack<PluginInventory> openedInventories = new Stack<>();
    private final MMOPlayerData playerData;
    private final Player player;

    private Inventory lastBukkitOpened;
    public BukkitTask backgroundTask;
    private boolean canClose = true, closed = true;

    /**
     * Temporarily disables the navigator event listeners without fully
     * unregistering it. Either a task has already been scheduled to open it
     * again, or the responsibility of opening it again has been delegated
     * to another class.
     * <p>
     * On initialization, listener is on hold until the first inventory is opened.
     */
    private boolean onHold = true;

    public Navigator(@NotNull Player player) {
        this(MMOPlayerData.get(player));
    }

    public Navigator(@NotNull MMOPlayerData playerData) {
        this.playerData = Objects.requireNonNull(playerData);
        this.player = playerData.getPlayer();
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
    }

    @NotNull
    public MMOPlayerData getMMOPlayerData() {
        return playerData;
    }

    public void blockClosing() {
        canClose = false;
    }

    public void unblockClosing() {
        canClose = true;
    }

    @NotNull
    public PluginInventory push(@NotNull PluginInventory inventory) {
        openedInventories.push(inventory);
        return inventory;
    }

    @NotNull
    public PluginInventory pushOpen(@NotNull PluginInventory inventory) {
        openedInventories.push(inventory);
        openLast();
        return inventory;
    }

    public boolean isClosed() {
        return closed;
    }

    @Nullable
    public Inventory getLastBukkitOpened() {
        return lastBukkitOpened;
    }

    @NotNull
    public PluginInventory peek() {
        return openedInventories.peek();
    }

    /**
     * Opens the upmost inventory in the stack
     *
     * @return Upmost inventory in the navigator
     */
    @Nullable
    public PluginInventory openLast() {
        final PluginInventory upmost = openedInventories.peek();
        if (!playerData.isOnline()) return upmost;

        final Inventory bukkitInventory = upmost.getInventory(); // Generate Bukkit inventory
        lastBukkitOpened = bukkitInventory;

        // Reopen listeners if necessary
        if (closed) {
            registerEvents();
            closed = false;
        }

        // Only then we open the inventory on sync
        if (Bukkit.isPrimaryThread()) openToPlayer(bukkitInventory);
        else Tasks.runSync(MythicLib.plugin, () -> openToPlayer(bukkitInventory));

        // Start task
        upmost.startBackgroundTask();

        return upmost;
    }

    private void openToPlayer(@NotNull Inventory bukkitInventory) {
        /*
         * This makes sure that closing the current
         * inventory does not close this navigator.
         */
        onHold = true;

        playerData.getPlayer().openInventory(bukkitInventory);

        /*
         * This makes sure that subsequent clicks and closes
         * will be registered once the inventory is opened.
         */
        onHold = false;
    }

    /**
     * Pops upmost inventory and opens the second-in-order
     */
    @NotNull
    public PluginInventory popOpen() {
        openedInventories.pop();
        return openLast();
    }

    public void haltBackgroundTask() {
        if (backgroundTask == null) return; // Task already halted

        backgroundTask.cancel();
        backgroundTask = null;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getPlayer().equals(player)) return;

        haltBackgroundTask();

        if (onHold) return; // On hold?

        // Cannot close
        if (!canClose) {
            onHold = true;
            final PluginInventory upmost = openedInventories.peek();
            Bukkit.getScheduler().runTaskLater(MythicLib.plugin, this::openLast, upmost.getCloseTimeOut());
            return;
        }

        // Trigger close
        close();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        if (onHold) return; // On hold?

        openedInventories.peek().onClick(event);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        if (onHold) return; // On hold?

        openedInventories.peek().onDrag(event);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().equals(player)) return;

        close();
    }

    private void close() {
        Validate.isTrue(!closed, "Already closed");
        closed = true;

        openedInventories.peek().onClose();

        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }
}
