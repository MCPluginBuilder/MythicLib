package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.util.IconOptions;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.def.passive.Backstab;
import io.lumine.mythic.lib.skill.parameter.SkillParameter;
import io.lumine.mythic.lib.skill.parameter.value.ScalingFormula;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
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
 * @param <T> Skill result class being used by that skill behavior
 * @author jules
 */
// TODO adapt skill handlers to take in ConfigWrappers, not only config sections. syntax will be alot like MythicMobs
public abstract class SkillHandler<T extends SkillResult> {
    private final String id, lowerCaseId, name;
    private final boolean triggerable;

    private final Map<String, SkillParameter> parameters = new HashMap<>();

    private final IconOptions icon;
    private final List<String> lore;
    private final List<String> categories;
    private final TriggerType defaultTriggerType;

    protected static final Random RANDOM = new Random();
    private static final IconOptions DEFAULT_ICON = new IconOptions(Material.BOOK);
    private static final List<String> BASE_MODIFIERS = List.of("cooldown", "mana", "stamina", "timer", "delay");

    public SkillHandler(@Nullable ConfigurationSection config) {

        /////////////////////////////////
        // Check if it is built-in
        /////////////////////////////////
        final var builtinAnnot = this.getClass().getAnnotation(BuiltinSkillHandler.class);
        this.id = inferSkillHandlerId(config);
        this.triggerable = builtinAnnot == null || builtinAnnot.triggerable();

        // Built-in skill parameters
        if (builtinAnnot != null) for (var mod : builtinAnnot.mods()) initializeModifier(mod, config);

        // Default skill parameters
        for (var mod : BASE_MODIFIERS) initializeModifier(mod, config);

        // Custom skill parameters
        if (builtinAnnot == null && config != null && config.isConfigurationSection("parameters"))
            for (var mod : config.getConfigurationSection("parameters").getKeys(false))
                initializeModifier(mod, config);

        // Basic
        this.lowerCaseId = this.id.toLowerCase();
        this.name = config != null ? Objects.requireNonNullElseGet(config.getString("name"), this::inferSkillName) : inferSkillName();
        this.icon = config != null && config.contains("icon") ? IconOptions.from(config.get("icon")) : DEFAULT_ICON;
        this.lore = config != null ? config.getStringList("lore") : List.of();

        // Default trigger type
        // For builtin non triggerable skills, it's API
        // For custom skills, defaults to CAST unless user specifies otherwise
        defaultTriggerType = !triggerable ? TriggerType.API
                : config == null ? TriggerType.CAST
                : UtilityMethods.prettyValueOf(TriggerType::valueOf, config.getString("trigger", "CAST"), "No trigger with name '%s'");

        // Categories
        categories = config != null ? config.getStringList("categories") : new ArrayList<>();
        categories.add(this.id);
        categories.add(defaultTriggerType.isPassive() ? "PASSIVE" : "ACTIVE");
    }

    @NotNull
    private String inferSkillHandlerId(@Nullable ConfigurationSection config) {
        if (config != null) return UtilityMethods.enumName(config.getName());
        //if (annot != null) return UtilityMethods.enumName(getClass().getSimpleName());
        return UtilityMethods.enumName(getClass().getSimpleName());
    }

    private String inferSkillName() {
        return UtilityMethods.caseOnWords(this.lowerCaseId.replace("_", " "));
    }

    private void initializeModifier(@NotNull String modifier, @Nullable ConfigurationSection config) {
        this.parameters.computeIfAbsent(modifier, m -> {
            if (config == null) return SkillParameter.empty(modifier);
            return SkillParameter.fromConfig(config.get("parameters." + modifier), m);
        });
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getLowerCaseId() {
        return lowerCaseId;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    @NotNull
    public IconOptions getIcon() {
        return icon;
    }

    @NotNull
    public List<String> getCategories() {
        return categories;
    }

    @NotNull
    public TriggerType getDefaultTriggerType() {
        return defaultTriggerType;
    }

    /**
     * This field is set to true to handle hard-coded passive
     * skills like {@link Backstab}. By convention and to serve
     * as an example for external developers, all hard coded
     * passive skills have to use the API trigger type.
     * <p>
     * This is the option default ML passive skills use.
     *
     * @return False if this skill should never trigger automatically
     *         without developer intervention through API calls
     */
    public boolean isTriggerable() {
        return triggerable;
    }

    public void addParameter(String parameter, @NotNull SkillParameter config) {
        parameters.put(parameter, config);
    }

    @NotNull
    public String getParameterName(String modifier) {
        final var mapping = parameters.get(modifier);
        return mapping != null ? mapping.getTranslate() : modifier;
    }

    public double getDefaultItemParameter(String modifier) {
        final var mapping = parameters.get(modifier);
        return mapping != null ? mapping.getItemDefaultValue() : 0;
    }

    @NotNull
    public ScalingFormula getDefaultFormula(String modifier) {
        final var mapping = parameters.get(modifier);
        return mapping != null ? mapping.getScalingFormula() : ScalingFormula.ZERO;
    }

    @NotNull
    public DecimalFormat getParameterDecimalFormat(String modifier) {
        final var mapping = parameters.get(modifier);
        return mapping != null ? mapping.getDecimalFormat() : SkillParameter.DEFAULT_DECIMAL_FORMAT;
    }

    /**
     * This registers modifiers with default configs i.e default
     * item value set to 0, no scaling formula, no special decimal format,
     * no translation, etc
     *
     * @param modifiers Modifier String identifiers
     */
    public void registerModifiers(@NotNull String... modifiers) {
        for (var modifier : modifiers) parameters.put(modifier, SkillParameter.empty(modifier));
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
    @NotNull
    public Set<String> getParameters() {
        return this.parameters.keySet();
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

    //region Deprecated

    @Deprecated
    public Set<String> getModifiers() {
        return getParameters();
    }

    @Deprecated
    public void registerModifiers(Collection<String> modifiers) {
        for (var mod : modifiers) registerModifiers(mod);
    }

    @Deprecated
    protected static final Random random = RANDOM;

    @Deprecated
    public SkillHandler() {
        this(new YamlConfiguration());
    }

    @Deprecated
    public SkillHandler(boolean triggerable) {
        this(new YamlConfiguration());
    }

    @Deprecated
    public SkillHandler(@NotNull String id) {
        this(new YamlConfiguration().createSection(id));
    }

    @Deprecated
    public List<String> getUiLore() {
        return lore;
    }

    //endregion
}
