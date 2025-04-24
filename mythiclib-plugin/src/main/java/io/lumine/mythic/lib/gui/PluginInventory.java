package io.lumine.mythic.lib.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.version.VersionUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class PluginInventory implements InventoryHolder {
    protected final Player player;
    protected final MMOPlayerData playerData;
    protected final Navigator navigator;

    // Pagination
    public int page = 1;

    // Runnable
    private Consumer<Inventory> backgroundRunnable;
    private long backgroundRunnablePeriod;

    public PluginInventory(MMOPlayerData playerData) {
        this(new Navigator(playerData));
    }

    public PluginInventory(Player player) {
        this(retrievePlayerData(player));
    }

    public PluginInventory(Navigator navigator) {
        this.playerData = navigator.getMMOPlayerData();
        this.player = playerData.getPlayer();
        this.navigator = navigator;

        navigator.push(this);
    }

    @NotNull
    public MMOPlayerData getMMOPlayerData() {
        return playerData;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Navigator getNavigator() {
        return navigator;
    }

    /**
     * Some inventories like the MMOProfiles profile selection UI cannot be closed
     * by the player before a certain action has occurred. This timeout is the delay
     * following which the inventory will be prompted again to the player.
     * <p>
     * This method can be overriden by other plugins to return the inventory close timeout.
     */
    public long getCloseTimeOut() {
        return 20;
    }

    public void registerRepeatingTask(Consumer<Inventory> repeatingTask, long repeatingTaskPeriod) {
        Validate.isTrue(this.backgroundRunnable == null, "A runnable already exists");

        this.backgroundRunnablePeriod = repeatingTaskPeriod;
        this.backgroundRunnable = repeatingTask;
    }

    public void startBackgroundTask() {
        if (backgroundRunnable == null) return; // No task to start

        Validate.isTrue(navigator.backgroundTask == null, "Background task already running");

        navigator.backgroundTask = Bukkit.getScheduler().runTaskTimer(MythicLib.plugin, () -> {
            Inventory opened = Objects.requireNonNull(VersionUtils.getOpen(player).getTopInventory());
            Inventory tracked = navigator.getLastBukkitOpened();

            // Should be the same physical objects
            if (opened != tracked) {
                navigator.haltBackgroundTask();
                throw new RuntimeException("Failed at keeping track of opened inventory");
            }

            backgroundRunnable.accept(tracked);
        }, backgroundRunnablePeriod, backgroundRunnablePeriod);
    }

    public void open() {
        Validate.isTrue(this == getNavigator().openLast(), "Opened an inventory that is not the last");
    }

    public abstract @NotNull Inventory getInventory();

    /**
     * Called when the inventory is clicked
     *
     * @param event Click event
     */
    public abstract void onClick(InventoryClickEvent event);

    public void onDrag(InventoryDragEvent event) {
        // Default implementation does nothing
    }

    /**
     * Called when the inventory is closed. No instance of event
     * is provided, because it can either be triggered by the player
     * closing the UI, or the player leaving the server
     */
    public void onClose() {
        // Default implementation does nothing
    }

    private static MMOPlayerData retrievePlayerData(Player player) {
        final Inventory open = VersionUtils.getOpen(player).getTopInventory();
        final InventoryHolder holder = UtilityMethods.getHolder(open);
        return holder instanceof PluginInventory ? ((PluginInventory) holder).playerData : MMOPlayerData.get(player);
    }
}
