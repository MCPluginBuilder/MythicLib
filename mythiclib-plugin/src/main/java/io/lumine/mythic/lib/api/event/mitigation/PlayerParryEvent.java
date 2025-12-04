package io.lumine.mythic.lib.api.event.mitigation;

import io.lumine.mythic.lib.api.event.DamageMitigationEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.mitigation.MitigationType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class PlayerParryEvent extends DamageMitigationEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final EntityDamageEvent event;
    private boolean cancelled;

    public PlayerParryEvent(MMOPlayerData player, EntityDamageEvent event, MitigationType type) {
        super(player, type, event);

        this.event = event;
    }

    public EntityDamageEvent getEvent() {
        return event;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
