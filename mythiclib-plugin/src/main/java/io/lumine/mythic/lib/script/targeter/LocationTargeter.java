package io.lumine.mythic.lib.script.targeter;

import io.lumine.mythic.lib.script.targeter.location.Orientable;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;

import java.util.List;

public abstract class LocationTargeter {
    private final boolean oriented;

    protected LocationTargeter(ConfigObject config) {
        this(config.getBoolean("oriented", false));
    }

    protected LocationTargeter(boolean oriented) {
        this.oriented = oriented;
        Validate.isTrue(!oriented || getClass().isAnnotationPresent(Orientable.class), "Tried creating an oriented location targeter with a non orientable type");
    }

    protected boolean isOriented() {
        return oriented;
    }

    public abstract List<Location> findTargets(SkillMetadata meta);
}
