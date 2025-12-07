package io.lumine.mythic.lib.skill.result;

import io.lumine.mythic.lib.skill.SkillMetadata;

/**
 * When the player tries to cast a skill either through
 * MMOCore or MMOItems, a skill result instance is created.
 * <p>
 * This instance determines if that skill can be cast under
 * the circumstances provided by the SkillMetadata.
 * <p>
 * Skill results share similar features with custom
 * skill conditions as well as skill targeters.
 */
@FunctionalInterface
public interface SkillResult {

    /**
     * @return If the ability was cast successfully. This method is used to apply
     * extra ability conditions (player must be on the ground, must look at an entity...)
     * @implNote Any calculation should be ideally made in the constructor,
     * or in the worst case, cached as to minimize the impact on performance
     * of this method. For this reason, no instance of metadata is provided as parameter.
     */
    public boolean isSuccessful();

    /**
     * @see #isSuccessful()
     * @deprecated
     */
    @Deprecated
    public default boolean isSuccessful(SkillMetadata ignored) {
        return isSuccessful();
    }
}
