package io.lumine.mythic.lib.script.condition.location;

import io.lumine.mythic.lib.script.condition.type.LocationCondition;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;

/**
 * Checks if the target location is in a specific world
 */
public class WorldCondition extends LocationCondition {
    private final String worldName;

    public WorldCondition(ConfigObject config) {
        super(config, true);

        worldName = config.getString("name");
    }

    @Override
    public boolean isMet(SkillMetadata meta, Location loc) {
        return loc.getWorld().getName().equalsIgnoreCase(worldName);
    }
}