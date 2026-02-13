package io.lumine.mythic.lib.comp.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Particle;

public class DamageParticleCapImpl extends DamageParticleCap<PacketEvent, PacketContainer> {

    public DamageParticleCapImpl(int tickLimit) {
        super(tickLimit);

        // Particle listener
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(MythicLib.plugin, PacketType.Play.Server.WORLD_PARTICLES) {

            @Override
            public void onPacketSending(PacketEvent event) {
                final var packet = event.getPacket();
                if (packet.getNewParticles().read(0).getParticle() != Particle.DAMAGE_INDICATOR) return;

                final var player = event.getPlayer();
                final var originalAmount = packet.getIntegers().read(0);
                DamageParticleCapImpl.this.onPacketSend(player, event, packet, originalAmount);
            }
        });
    }

    @Override
    protected void cancelEvent(PacketEvent event) {
        event.setCancelled(true);
    }

    @Override
    protected void writeNewAmount(PacketContainer packet, int amount) {
        packet.getIntegers().write(0, amount);
    }
}
