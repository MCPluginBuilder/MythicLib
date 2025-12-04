package io.lumine.mythic.lib.api.event.mitigation;

import io.lumine.mythic.lib.api.event.DamageMitigationEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.mitigation.MitigationType;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class PlayerBlockEvent extends DamageMitigationEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final EntityDamageEvent event;
    private double power;
    private boolean cancelled;

    @Deprecated
    public PlayerBlockEvent(MMOPlayerData player, EntityDamageEvent event, MitigationType type) {
        super(player, type, event);

        this.event = event;
        this.power = power;
    }

    @Deprecated
    public EntityDamageEvent getEvent() {
        return event;
    }

    @Deprecated
    public double getPower() {
        return power;
    }

    @Deprecated
    public double getDamageBlocked() {
        return power * event.getFinalDamage();
    }

    @Deprecated
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Deprecated
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Deprecated
    public void setPower(double power) {
        Validate.isTrue(power <= 1 && power >= 0, "Block power must be between 0 and 1");
        this.power = power;
    }

    @Deprecated
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Deprecated
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

