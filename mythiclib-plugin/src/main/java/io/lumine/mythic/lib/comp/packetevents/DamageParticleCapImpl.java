package io.lumine.mythic.lib.comp.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import io.lumine.mythic.lib.comp.protocollib.DamageParticleCap;
import org.bukkit.entity.Player;

public class DamageParticleCapImpl extends DamageParticleCap<PacketSendEvent, WrapperPlayServerParticle> {

    public DamageParticleCapImpl(int tickLimit) {
        super(tickLimit);

        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketSend(PacketSendEvent event) {
                if (event.getPacketType() != PacketType.Play.Server.PARTICLE) return;
                final var packet = new WrapperPlayServerParticle(event);
                if (!packet.getParticle().getType().equals(ParticleTypes.DAMAGE_INDICATOR)) return;
                final var player = (Player) event.getPlayer();
                DamageParticleCapImpl.this.onPacketSend(player, event, packet, packet.getParticleCount());
            }
        }, PacketListenerPriority.NORMAL);
    }

    @Override
    protected void cancelEvent(PacketSendEvent event) {
        event.setCancelled(true);
    }

    @Override
    protected void writeNewAmount(WrapperPlayServerParticle packet, int amount) {
        packet.setParticleCount(amount);
    }
}
