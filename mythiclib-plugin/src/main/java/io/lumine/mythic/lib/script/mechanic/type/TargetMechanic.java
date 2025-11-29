package io.lumine.mythic.lib.script.mechanic.type;

import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.script.targeter.entity.DefaultEntityTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * A mechanic that takes an entity as parameter. Examples:
 * - damaging mechanics
 * - potion effects
 * - heal, feed, saturate mechanics
 * - tell mechanic
 */
public abstract class TargetMechanic extends Mechanic {
    private final EntityTargeter targeter;

    public TargetMechanic(ConfigObject config) {
        targeter = config.contains("target") ? config.getEntityTargeter("target") : new DefaultEntityTargeter();
    }

    public EntityTargeter getTargeter() {
        return targeter;
    }

    public void cast(@NotNull SkillMetadata meta) {
        for (Entity target : targeter.findTargets(meta))
            cast(meta, target);
    }

    public abstract void cast(SkillMetadata meta, Entity target);
}
