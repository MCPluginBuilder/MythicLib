package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.def.passive.Backstab;
import io.lumine.mythic.lib.skill.result.SkillResult;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The interface that groups up:
 * - custom MM skills
 * - custom MythicLib skills
 * - custom Fabled skills
 * - default MythicLib skills
 * <p>
 * MythicLib has a huge default collection of "skills" however
 * these cannot be cast yet because they still lack important
 * properties which cannot be setup in MythicLib because they
 * are specific to the plugins using the ML skills:
 * - the ability configurable name and description (different setups for MI and MMOCore)
 * - default values for modifiers (also different formulas for MI and MMOCore)
 * <p>
 * MythicLib stores {@link SkillHandler}, which are skills substracted from
 * all of this data which is SPECIFIC to the plugin using the skills.
 * Other plugins like MMOCore and MMOItems store {@link Skill} instances.
 *
 * @param <T> Skill result class being used by that skill behaviour
 * @author jules
 */
// TODO adapt skill handlers to take in ConfigWrappers, not only config sections. syntax will be alot like MythicMobs
public abstract class SkillHandler<T extends SkillResult> {
    private final String id;
    private final Set<String> parameters = new HashSet<>();
    private final boolean triggerable;

    protected static final Random RANDOM = new Random();

    @Deprecated
    protected static final Random random = RANDOM;

    /**
     * Used by default MythicLib skill handlers
     */
    public SkillHandler() {
        this(true);
    }

    /**
     * Used by default MythicLib skill handlers
     *
     * @param triggerable If the skill can be triggered
     */
    public SkillHandler(boolean triggerable) {
        this.id = UtilityMethods.enumName(getClass().getSimpleName());
        this.triggerable = triggerable;

        registerModifiers("cooldown", "mana", "stamina", "timer", "delay");
    }

    /**
     * Used to register a custom skill handler
     *
     * @param id Skill handler identifier
     */
    public SkillHandler(@NotNull String id) {
        this(null, id);
    }

    /**
     * Used to register a custom skill handler
     *
     * @param config Configuration section to load the skill handler from
     * @param id     Skill handler identifier
     */
    public SkillHandler(@Nullable ConfigurationSection config, @NotNull String id) {
        this.id = UtilityMethods.enumName(id);
        this.triggerable = true;

        // Register custom modifiers
        if (config != null && config.contains("modifiers"))
            registerModifiers(config.getStringList("modifiers"));

        // Default modifiers
        registerModifiers("cooldown", "mana", "stamina", "timer", "delay");
    }

    public String getId() {
        return id;
    }

    public String getLowerCaseId() {
        return id.toLowerCase().replace("_", "-");
    }

    public void registerModifiers(String... mods) {
        registerModifiers(Arrays.asList(mods));
    }

    public void registerModifiers(Collection<String> mods) {
        parameters.addAll(mods);
    }

    /**
     * This field is set to true to handle hard coded passive
     * skills like {@link Backstab}. By convention and to serve
     * as an example for external developers, all hard coded
     * passive skills have to use the API trigger type.
     * <p>
     * This is the option default ML passive skills use.
     *
     * @return If it should be triggered when calling {@link MMOPlayerData#triggerSkills(io.lumine.mythic.lib.skill.trigger.TriggerMetadata)}
     */
    public boolean isTriggerable() {
        return triggerable;
    }

    /**
     * @deprecated Skill modifiers are now called "parameters"
     */
    @Deprecated
    public Set<String> getModifiers() {
        return getParameters();
    }

    /**
     * Skill parameters are specific numerical values that
     * determine how powerful a skill is. Parameters can be
     * the skill damage, cooldown, duration if it applies
     * some potion effect, etc.
     * <p>
     * MythicLib does NOT store default parameter values/
     * formulas that scale with the player class level. It
     * rather only stores what modifiers the skill has, as
     * it's the only necessary information for skill handlers.
     *
     * @return The set of all possible parameters of that skill
     */
    public Set<String> getParameters() {
        return parameters;
    }

    /**
     * Skill results are used to check if a skill can be cast.
     * <p>
     * This method evaluates MythicMobs custom conditions,
     * checks if the caster has an entity in their line of
     * sight, if he is on the ground...
     * <p>
     * Runs first before {@link Skill#getResult(SkillMetadata)}
     *
     * @param meta Info of skill being cast
     * @return Skill result
     */
    @NotNull
    public abstract T getResult(SkillMetadata meta);

    /**
     * This is where the actual skill effects are applied.
     * <p>
     * Runs last, after {@link Skill#whenCast(SkillMetadata)}
     *
     * @param result    Skill result
     * @param skillMeta Info of skill being cast
     */
    public abstract void whenCast(T result, SkillMetadata skillMeta);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillHandler<?> that = (SkillHandler<?>) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
