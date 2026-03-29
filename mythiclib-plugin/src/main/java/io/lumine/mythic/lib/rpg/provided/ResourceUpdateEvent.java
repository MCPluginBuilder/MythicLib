package io.lumine.mythic.lib.rpg.provided;


import io.lumine.mythic.lib.api.event.MMOPlayerDataEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ResourceUpdateEvent extends MMOPlayerDataEvent implements Cancellable {
    private final PlayerResource type;
    private final ResourceUpdateReason reason;

    private final double oldAmount;
    private double newAmount;
    private boolean cancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * When resource is regained for any reason by a player
     *
     * @param player    Player regaining resource
     * @param oldAmount Old amount, immutable
     * @param newAmount New amount
     * @param reason    Reason of regain
     * @param type      Type of resource, either MANA or STAMINA
     */
    public ResourceUpdateEvent(MMOPlayerData player, double oldAmount, double newAmount, ResourceUpdateReason reason, PlayerResource type) {
        super(player);

        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
        this.reason = reason;
        this.type = type;
    }

    public double getOldAmount() {
        return oldAmount;
    }

    public double getNewAmount() {
        return newAmount;
    }

    public void setNewAmount(double newAmount) {
        this.newAmount = newAmount;
    }

    public ResourceUpdateReason getReason() {
        return this.reason;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean value) {
        this.cancelled = value;
    }

    public PlayerResource getType() {
        return type;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
