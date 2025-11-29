package io.lumine.mythic.lib.script.mechanic;

import io.lumine.mythic.lib.skill.SkillMetadata;
import org.jetbrains.annotations.NotNull;

/**
 * A mechanic is an elementary brick of a skill. It can
 * be an action like casting a command or something more
 * complex like defining and casting a projectile.
 * <p>
 * Mechanics can cast other mechanics, interact with variables
 * in order to create complex and FULLY CUSTOM skills
 */
public abstract class Mechanic {
    public abstract void cast(@NotNull SkillMetadata meta);
}
