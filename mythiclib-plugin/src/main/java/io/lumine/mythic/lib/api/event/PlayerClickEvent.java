package io.lumine.mythic.lib.api.event;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerClickEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final EquipmentSlot hand;
    private final boolean leftClick;

    public PlayerClickEvent(@NotNull Player player, @NotNull EquipmentSlot hand, boolean leftClick) {
        super(player);

        this.hand = hand;
        this.leftClick = leftClick;
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
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
