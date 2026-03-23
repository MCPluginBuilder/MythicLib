package io.lumine.mythic.lib.api.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.condition.type.MMOCondition;
import org.bukkit.World;

public class WorldCondition extends MMOCondition implements io.lumine.mythic.lib.api.condition.type.WorldCondition {
    private final String world;

    public WorldCondition(MMOLineConfig config) {
        super(config);

        this.world = config.getString("name");
    }

    @Override
    public boolean check(World world) {
        return world.getName().equals(this.world);
    }
}

