package io.lumine.mythic.lib.api.event;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerClickEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Cancellable bukkitEvent;
    private final EquipmentSlot hand;
    private final boolean leftClick;
    private final Block clickedBlock;
    private final ItemStack item;

    public PlayerClickEvent(@NotNull Player player, @NotNull EquipmentSlot hand, boolean leftClick, @Nullable Block clickedBlock, @Nullable ItemStack item, @NotNull Cancellable bukkitEvent) {
        super(player);

        this.hand = hand;
        this.leftClick = leftClick;
        this.clickedBlock = clickedBlock;
        this.item = item;
        this.bukkitEvent = bukkitEvent;
    }

    public boolean hasBlock() {
        return clickedBlock != null;
    }

    @Nullable
    public Block getClickedBlock() {
        return clickedBlock;
    }

    public boolean hasItem() {
        return item != null;
    }

    @Nullable
    public ItemStack getItem() {
        return item;
    }

    @Override
    public boolean isCancelled() {
        if (bukkitEvent instanceof PlayerInteractEvent) return ((PlayerInteractEvent) bukkitEvent).useItemInHand() == Result.DENY;
        return bukkitEvent.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        if (bukkitEvent instanceof PlayerInteractEvent) ((PlayerInteractEvent) bukkitEvent).setUseItemInHand(Result.DENY);
        else bukkitEvent.setCancelled(b);
    }

    @NotNull
    public EquipmentSlot getHand() {
        return hand;
    }

    public boolean isLeftClick() {
        return leftClick;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
