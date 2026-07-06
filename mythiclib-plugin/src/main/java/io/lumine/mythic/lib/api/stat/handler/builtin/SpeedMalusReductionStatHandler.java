package io.lumine.mythic.lib.api.stat.handler.builtin;

import io.lumine.mythic.lib.api.stat.SharedStat;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

// TODO does anybody even use this stat
public class SpeedMalusReductionStatHandler extends StatHandler {
    private final MovementSpeedStatHandler delegate;

    public SpeedMalusReductionStatHandler(@NotNull ConfigurationSection config, MovementSpeedStatHandler delegate) {
        super(config, SharedStat.SPEED_MALUS_REDUCTION);

        this.delegate = delegate;
    }

    @Override
    public void broadcastValueUpdate(@NotNull StatInstance instance) {
        // Update Movement speed again
        instance.getMap().getInstance(delegate.getStat()).update();
    }
}
