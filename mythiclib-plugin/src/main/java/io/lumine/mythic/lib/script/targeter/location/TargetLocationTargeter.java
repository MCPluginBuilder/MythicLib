package io.lumine.mythic.lib.script.targeter.location;

import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

public class TargetLocationTargeter extends LocationTargeter {
    public TargetLocationTargeter() {
        super(false);
    }

    @Override
    public List<Location> findTargets(SkillMetadata meta) {
        return Collections.singletonList(meta.getTargetLocation());
    }
}
