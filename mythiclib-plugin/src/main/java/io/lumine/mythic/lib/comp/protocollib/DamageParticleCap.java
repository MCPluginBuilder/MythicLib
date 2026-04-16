package io.lumine.mythic.lib.comp.protocollib;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class DamageParticleCap<E, P> {

    /**
     * Maximum amount of particles sent per tick
     */
    private final int tickLimit;

    public DamageParticleCap(int tickLimit) {
        this.tickLimit = tickLimit;
    }

    protected abstract void cancelEvent(E event);

    protected abstract void writeNewAmount(P packet, int amount);

    public void onPacketSend(Player player, E event, P packet, int originalAmount) {

        // Shortcut to reduce performance footprint
        if (tickLimit == 0) {
            this.cancelEvent(event);
            return;
        }

        final var playerData = MMOPlayerData.getOrNull(player.getUniqueId());
        if (playerData == null) return;

        // Atomically update counter
        final var prevCounter = playerData.damageParticleCount.getAndAdd(originalAmount);
        final var effective = Math.min(originalAmount, Math.max(0, tickLimit - prevCounter));

        // Skip packet if no particles are to be sent
        if (effective <= 0) {
            this.cancelEvent(event);
            return;
        }

        // Runs on the next tick
        // To avoid problems of counters that don't go down, just set back to 0
        Bukkit.getScheduler().runTaskAsynchronously(MythicLib.plugin, () -> playerData.damageParticleCount.set(0));

        // Set the new particle amount if needed
        if (effective != originalAmount) this.writeNewAmount(packet, effective);
    }
}
