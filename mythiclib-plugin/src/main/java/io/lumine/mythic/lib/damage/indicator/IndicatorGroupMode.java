package io.lumine.mythic.lib.damage.indicator;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageMetadata;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public enum IndicatorGroupMode {

    /**
     * Only one indicator for the full attack. All damage
     * types are all put together across damage packets.
     */
    SINGLE(damage -> {
        final var indicator = new DamageIndicator(damage.getDamage(), damage.collectTypes());
        return List.of(indicator);
    }),

    /**
     * Per packet splitting
     */
    TYPE(damage -> {
        var list = new ArrayList<DamageIndicator>();
        for (var dtype : MythicLib.plugin.getDamageIndicators().getDamageTypeSplits()) {
            list.add(new DamageIndicator(damage.getDamage(dtype), List.of(dtype)));
        }
        return list;
    }),

    /**
     * One indicator per packet
     */
    PACKET(damage -> {
        var list = new ArrayList<DamageIndicator>();
        for (var packet : damage.getPackets())
            list.add(new DamageIndicator(packet.getFinalValue(), packet.getTypes(), packet.getElement()));
        return list;
    });

    private final Function<DamageMetadata, List<DamageIndicator>> generator;

    IndicatorGroupMode(Function<DamageMetadata, List<DamageIndicator>> generator) {
        this.generator = generator;
    }

    @NotNull
    public List<DamageIndicator> getIndicators(DamageMetadata damage) {
        return generator.apply(damage);
    }
}
