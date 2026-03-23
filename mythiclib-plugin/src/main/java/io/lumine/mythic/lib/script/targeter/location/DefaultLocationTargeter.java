package io.lumine.mythic.lib.script.targeter.location;

import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

/**
 * Takes target location or source location if null/prioritized
 */
public class DefaultLocationTargeter extends LocationTargeter {
    public DefaultLocationTargeter() {
        super(false);
    }

    @Override
    public List<Location> findTargets(SkillMetadata meta) {
        return Collections.singletonList(meta.getSkillLocation(false));
    }
}
