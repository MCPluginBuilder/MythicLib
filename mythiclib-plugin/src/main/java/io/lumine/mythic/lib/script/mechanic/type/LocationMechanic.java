package io.lumine.mythic.lib.script.mechanic.type;

import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.targeter.location.DefaultLocationTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * A mechanic that takes a location as parameter. Examples:
 * - particle and sound mechanics
 * - lightning mechanic
 */
public abstract class LocationMechanic extends Mechanic {
    private final LocationTargeter targeter;

    public LocationMechanic(ConfigObject config) {
        this.targeter = config.contains("target") ? config.getLocationTargeter("target") : new DefaultLocationTargeter();
    }

    public LocationTargeter getTargeter() {
        return targeter;
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        for (Location loc : targeter.findTargets(meta))
            cast(meta, loc);
    }

    public abstract void cast(SkillMetadata meta, Location loc);
}
