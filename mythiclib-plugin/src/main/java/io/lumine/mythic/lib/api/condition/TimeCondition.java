package io.lumine.mythic.lib.api.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.condition.type.MMOCondition;
import org.bukkit.World;

public class TimeCondition extends MMOCondition implements io.lumine.mythic.lib.api.condition.type.WorldCondition {
    private final int minTime, maxTime;

    public TimeCondition(MMOLineConfig config) {
        super(config);

        minTime = convertTime(config.getString("min"));
        maxTime = convertTime(config.getString("max"));
    }

    private int convertTime(String time) {
        if (time.matches("\\d+"))
            return Math.min(24000, Math.max(0, Integer.parseInt(time)));

        switch (time.toLowerCase()) {
            case "day":
                return 1000;
            case "noon":
                return 6000;
            case "sunset":
                return 12000;
            case "night":
                return 13000;
            case "midnight":
                return 18000;
            case "sunrise":
                return 23000;
        }

        return -1;
    }

    @Override
    public boolean check(World world) {
        return world.getTime() > minTime && world.getTime() < maxTime;
    }
}
