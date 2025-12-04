package io.lumine.mythic.lib.api.event;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.onhit.OnHitEffect;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class OnHitEffectEvent extends MMOPlayerDataEvent implements Cancellable {
    private final EntityDamageEvent bukkitEvent;
    private final OnHitEffect effect;

    private boolean cancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public OnHitEffectEvent(@NotNull MMOPlayerData player, @NotNull OnHitEffect effect, @NotNull EntityDamageEvent bukkitEvent) {
        super(player);

        this.bukkitEvent = bukkitEvent;
        this.effect = effect;
    }

    @NotNull
    public EntityDamageEvent getBukkitEvent() {
        return bukkitEvent;
    }

    @NotNull
    public OnHitEffect getEffect() {
        return effect;
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
