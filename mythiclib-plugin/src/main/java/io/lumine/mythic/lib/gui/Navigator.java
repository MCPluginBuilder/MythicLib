package io.lumine.mythic.lib.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.Tasks;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.VersionUtils;
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

    @Nullable
    private PluginInventory lastInvOpened;
    @Nullable
    private Inventory lastBukkitOpened;
    @Nullable
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

    // TODO improve code
    public boolean recycle;

    /**
     * Opens the upmost inventory in the stack
     *
     * @return Upmost inventory in the navigator, or null
     *         if inventory could not be opened.
     */
    @Nullable
    public PluginInventory openLast() {

        // Safeguard. Do not open inventory
        if (!playerData.isOnline()) return null;

        final var upmost = openedInventories.peek();
        upmost.onOpen(); // Notify inventory open

        if (this.recycle) {
            this.recycle = false;
            return upmost;
        }

        // Close current inventory if any
        if (lastInvOpened != null) closeCurrentInventory();

        final var newBukkitInventory = upmost.getInventory(); // Generate Bukkit inventory
        this.lastInvOpened = upmost;
        this.lastBukkitOpened = newBukkitInventory;

        // Reopen listeners if necessary
        if (closed) {
            registerEvents();
            closed = false;
        }

        // Only then we open the inventory sync
        Tasks.runSync(MythicLib.plugin, () -> openToPlayer(newBukkitInventory));

        startBackgroundTask(upmost); // Start background task

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
     *
     * @return The new upmost inventory, or null if inventory stack is empty
     */
    @Nullable
    public PluginInventory popOpen() {
        openedInventories.pop();
        if (openedInventories.isEmpty()) {
            player.closeInventory();
            return null;
        }
        return openLast();
    }

    private void close() {
        Validate.isTrue(!closed, "Already closed");
        closed = true;

        if (lastInvOpened != null) closeCurrentInventory();

        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    //region Open and close inventory

    private void closeCurrentInventory() {
        Validate.notNull(lastInvOpened, "No inventory open");

        lastInvOpened.onClose();
        haltBackgroundTask();
        this.lastInvOpened = null;
    }

    private void startBackgroundTask(@NotNull PluginInventory nextOpened) {
        final var backgroundRunnable = nextOpened.getBackgroundRunnable();
        if (backgroundRunnable == null) return; // No task to start

        Validate.isTrue(backgroundTask == null, "Background task already running");

        backgroundTask = Bukkit.getScheduler().runTaskTimer(MythicLib.plugin, () -> {
            final var opened = Objects.requireNonNull(VersionUtils.getOpen(player).getTopInventory());
            final var tracked = getLastBukkitOpened();

            // Should be the same physical objects
            if (opened != tracked) {
                Navigator.this.haltBackgroundTask();
                throw new RuntimeException("Failed at keeping track of opened inventory");
            }

            backgroundRunnable.accept(tracked);
        }, nextOpened.getBackgroundRunnablePeriod(), nextOpened.getBackgroundRunnablePeriod());
    }

    private void haltBackgroundTask() {
        if (backgroundTask == null) return; // Task already halted

        backgroundTask.cancel();
        backgroundTask = null;
    }

    //endregion

    //region Listeners

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getPlayer().equals(player)) return;
        if (onHold) return;

        if (canClose) close();
        else {
            onHold = true;
            final var upmost = openedInventories.peek();
            Bukkit.getScheduler().runTaskLater(MythicLib.plugin, this::openLast, upmost.getCloseTimeOut());
        }
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

    //endregion Listeners
}
