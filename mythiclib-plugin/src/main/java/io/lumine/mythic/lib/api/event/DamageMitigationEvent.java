package io.lumine.mythic.lib.api.event;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.mitigation.MitigationType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class DamageMitigationEvent extends MMOPlayerDataEvent implements Cancellable {
    private final EntityDamageEvent bukkitEvent;
    private final MitigationType mechanic;

    private boolean cancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public DamageMitigationEvent(@NotNull MMOPlayerData player, @NotNull MitigationType mechanic, @NotNull EntityDamageEvent bukkitEvent) {
        super(player);

        this.bukkitEvent = bukkitEvent;
        this.mechanic = mechanic;
    }

    @NotNull
    public EntityDamageEvent getBukkitEvent() {
        return bukkitEvent;
    }

    @NotNull
    public MitigationType getMechanic() {
        return mechanic;
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
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
