package io.lumine.mythic.lib.script.targeter.location;

import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.EntityLocationType;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

/**
 * Takes the location of the target entity. Just like {@link CasterLocationTargeter}
 * you can provide the position of the entity body you want to use as location
 */
public class TargetEntityLocationTargeter extends LocationTargeter {
    private final EntityLocationType entityLocationType;

    public TargetEntityLocationTargeter(ConfigObject config) {
        super(false);

        this.entityLocationType = config.contains("position") ? EntityLocationType.valueOf(config.getString("position").toUpperCase()) : EntityLocationType.BODY;
    }

    @Override
    public List<Location> findTargets(SkillMetadata meta) {
        return Arrays.asList(entityLocationType.getLocation(meta.getTargetEntity()));
    }
}
