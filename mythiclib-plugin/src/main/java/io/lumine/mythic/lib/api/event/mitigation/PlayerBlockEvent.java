package io.lumine.mythic.lib.api.event.mitigation;

import io.lumine.mythic.lib.api.event.MMOPlayerDataEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerBlockEvent extends MMOPlayerDataEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final EntityDamageEvent event;
    private double power;
    private boolean cancelled;

    public PlayerBlockEvent(MMOPlayerData player, EntityDamageEvent event, double power) {
        super(player);

        this.event = event;
        this.power = power;
    }

    public EntityDamageEvent getEvent() {
        return event;
    }

    public double getPower() {
        return power;
    }

    public double getDamageBlocked() {
        return power * event.getFinalDamage();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setPower(double power) {
        Validate.isTrue(power <= 1 && power >= 0, "Block power must be between 0 and 1");
        this.power = power;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

