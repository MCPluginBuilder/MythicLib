package io.lumine.mythic.lib.player.resource;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * When MMOCore is installed, it calls an event when
 * the player sees any of their resources (health, mana...)
 * updated.
 * <p>
 * Since MythicLib and MMOItems also need access to heal
 * methods, this requires an interface in MythicLib.
 */
public interface HealthUpdateEventSupplier<T extends Event & AbstractHealthUpdateEvent> {

    @NotNull
    public T onHealthUpdate(@NotNull Player player, double oldAmount, double newAmount, @NotNull ResourceUpdateReason updateReason);
}
