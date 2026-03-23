package io.lumine.mythic.lib.skill;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.script.util.VariableNotFoundException;
import io.lumine.mythic.lib.script.variable.Variable;
import io.lumine.mythic.lib.script.variable.VariableList;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.script.variable.def.*;
import io.lumine.mythic.lib.util.EntityLocationType;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.SkillOrientation;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instantiated every time a player casts a skill/script. This
 * contains all the required temporary data like the skill caster
 * and the cached statistics.
 * <p>
 * This also stores variables which can be edited and manipulated by the user.
 *
 * @author jules
 */
public class SkillMetadata {
    private final VariableList vars;

    @Nullable
    private final Skill cast;

    /**
     * Entity by which the skill was cast
     */
    @NotNull
    private final PlayerMetadata caster;

    /**
     * Location at which the skill was cast
     */
    @NotNull
    private final Location origin;

    /**
     * Some skills like projectiles or ray casts cache
     * a target entity which is later used in targeters
     */
    @Nullable
    private final Entity targetEntity;

    /**
     * Some skills like ray casts cache a target
     * location which is later used in targeters
     */
    @Nullable
    private final Location targetLocation;

    @Nullable
    private final Event sourceEvent;

    @Nullable
    private final AttackMetadata attackSource;

    @Nullable
    public final SkillOrientation orientation;

    /**
     * @param cast           Initial skill being cast, if any
     * @param caster         Cached statistics of the skill caster
     * @param vars           Skill variable list if it already exists
     * @param origin         The location at which the skill/mechanic was cast
     * @param targetLocation The skill/mechanic target location
     * @param targetEntity   The skill/mechanic target entity
     * @param orientation    Skill orientation if some rotation is required later on
     * @param attackSource   Attack which triggered the skill
     */
    public SkillMetadata(@Nullable Skill cast,
                         @NotNull PlayerMetadata caster,
                         @NotNull VariableList vars,
                         @NotNull Location origin,
                         @Nullable Location targetLocation,
                         @Nullable Entity targetEntity,
                         @Nullable SkillOrientation orientation,
                         @Nullable AttackMetadata attackSource,
                         @Nullable Event sourceEvent) {
        this.cast = cast;
        this.caster = caster;
        this.vars = vars;
        this.origin = origin;
        this.targetLocation = targetLocation;
        this.targetEntity = targetEntity;
        this.orientation = orientation;
        this.attackSource = attackSource;
        this.sourceEvent = sourceEvent;
    }

    @Nullable
    public Skill getCast() {
        return Objects.requireNonNull(cast, "No skill provided");
    }

    @NotNull
    public VariableList getVariableList() {
        return vars;
    }

    @NotNull
    public PlayerMetadata getCaster() {
        return caster;
    }

    @NotNull
    public Location getSourceLocation() {
        return origin.clone();
    }

    public boolean hasAttackSource() {
        return attackSource != null;
    }

    /**
     * @return The attack which triggered the skill.
     */
    @NotNull
    public AttackMetadata getAttackSource() {
        return Objects.requireNonNull(attackSource, "Skill was not triggered by any attack");
    }

    /**
     * Retrieves a specific skill parameter value. This applies to the
     * original skill being cast, which will work for most MMOCore and
     * MMOItems uses but might cause issues when dealing with custom scripts.
     *
     * @param parameter Skill parameter name
     * @return Skill parameter final value, taking into account skill mods
     */
    public double getParameter(@NotNull String parameter) {
        Validate.notNull(cast, "No skill provided");
        return caster.getData().getSkillModifierMap().calculateValue(cast, parameter);
    }

    @NotNull
    public Entity getTargetEntity() {
        return Objects.requireNonNull(targetEntity, "Skill has no target entity");
    }

    @Nullable
    public Entity getTargetEntityOrNull() {
        return targetEntity;
    }

    public boolean hasTargetEntity() {
        return targetEntity != null;
    }

    @NotNull
    public Location getTargetLocation() {
        return Objects.requireNonNull(targetLocation, "Skill has no target location").clone();
    }

    @Nullable
    public Location getTargetLocationOrNull() {
        return targetLocation == null ? null : targetLocation.clone();
    }

    public boolean hasTargetLocation() {
        return targetLocation != null;
    }

    @NotNull
    public SkillOrientation getOrientation() {
        return Objects.requireNonNull(orientation, "Skill has no orientation");
    }

    @Nullable
    public SkillOrientation getOrientationOrNull() {
        return orientation;
    }

    @NotNull
    public Event getSourceEvent() {
        return Objects.requireNonNull(sourceEvent, "Skill has no source event");
    }

    public boolean hasOrientation() {
        return orientation != null;
    }

    /**
     * Analog of {@link #getSkillEntity(boolean)}. Used when a skill requires a
     * location when no targeter is provided
     *
     * @param sourceLocation If the source location should be prioritized
     * @return Target location (and if it exists) OR location of target entity (and if it exists), source location otherwise
     */
    @NotNull
    public Location getSkillLocation(boolean sourceLocation) {
        return sourceLocation ? origin.clone() : targetLocation != null ? targetLocation.clone() : targetEntity != null ? EntityLocationType.BODY.getLocation(targetEntity) : origin.clone();
    }

    /**
     * Analog of {@link #getSkillLocation(boolean)}. Used when a skill requires an
     * entity when no targeter is provided
     *
     * @param caster If the skill caster should be prioritized
     * @return Target entity if prioritized (and if it exists), skill caster otherwise
     */
    @NotNull
    public Entity getSkillEntity(boolean caster) {
        return caster || targetEntity == null ? getCaster().getPlayer() : targetEntity;
    }

    /**
     * Analog of {@link #getSkillEntity(boolean)} or {@link #getSkillLocation(boolean)}
     * being used when a location targeter requires an orientation in order
     * to potentially orient locations.
     *
     * @return Skill orientation if not null. If it is, it tries to create
     *         one using the skill target and source location if it is not null.
     *         Throws a NPE if the metadata has neither an orientation nor a target location.
     */
    @NotNull
    public SkillOrientation getSkillOrientation() {
        return orientation != null ? orientation : new SkillOrientation(Objects.requireNonNull(targetLocation, "Skill has no orientation").clone(), targetLocation.clone().subtract(origin).toVector());
    }

    //region Cloning and Editing

    @NotNull
    public SkillMetadata clone(@NotNull Location targetLocation) {
        return clone(cast, origin, targetLocation, targetEntity, orientation);
    }

    @NotNull
    public SkillMetadata clone(@NotNull Skill cast) {
        return clone(cast, origin, targetLocation, targetEntity, orientation);
    }

    @NotNull
    public SkillMetadata withCaster(@NotNull PlayerMetadata caster) {
        return new SkillMetadata(this.cast, Objects.requireNonNull(caster, "Caster cannot be null"), this.vars, this.origin, this.targetLocation, this.targetEntity, this.orientation, this.attackSource, this.sourceEvent);
    }

    @NotNull
    public SkillMetadata withOrigin(@NotNull Location origin) {
        return new SkillMetadata(this.cast, this.caster, this.vars, Objects.requireNonNull(origin, "Origin cannot be null"), this.targetLocation, this.targetEntity, this.orientation, this.attackSource, this.sourceEvent);
    }

    @NotNull
    public SkillMetadata withTargetEntity(@Nullable Entity targetEntity) {
        return new SkillMetadata(this.cast, this.caster, this.vars, this.origin, this.targetLocation, targetEntity, this.orientation, this.attackSource, this.sourceEvent);
    }

    @NotNull
    public SkillMetadata withTargetLocation(@Nullable Location targetLocation) {
        return new SkillMetadata(this.cast, this.caster, this.vars, this.origin, targetLocation, this.targetEntity, this.orientation, this.attackSource, this.sourceEvent);
    }

    @NotNull
    public SkillMetadata clone(@NotNull Location source,
                               @Nullable Location targetLocation,
                               @Nullable Entity targetEntity) {
        return clone(cast, source, targetLocation, targetEntity, orientation);
    }

    @NotNull
    public SkillMetadata clone(@NotNull Location source,
                               @Nullable Location targetLocation,
                               @Nullable Entity targetEntity,
                               @Nullable SkillOrientation orientation) {
        return clone(cast, source, targetLocation, targetEntity, orientation);
    }

    @NotNull
    public SkillMetadata clone(@Nullable Skill cast,
                               @NotNull Location source,
                               @Nullable Location targetLocation,
                               @Nullable Entity targetEntity,
                               @Nullable SkillOrientation orientation) {
        return new SkillMetadata(cast, caster, vars, source, targetLocation, targetEntity, orientation, attackSource, sourceEvent);
    }

    //endregion

    public static final List<String> RESERVED_VARIABLE_NAMES = Arrays.asList("modifier", "parameter", "source", "targetLocation",
            "targetLoc", "target_loc", "target_location", "targetloc", "targetl", "caster", "attack", "stat", "target", "var",
            "rand", "random", "rdm");

    /**
     * User variables have scopes, which dictate in which variable registry
     * they are saved. They include (from highest to lowest priority in case
     * of name collision):
     * - SKILL
     * - PLAYER
     * - SERVER
     * <p>
     * By definition, all reserved variables are of SKILL scope. This method
     * looks through these variable registries in order to find the user
     * variable with the given name.
     *
     * @param name User variable name
     * @return User variable if found, throws a NPE otherwise.
     */
    @NotNull
    public Variable<?> getUserVariable(String name) {

        // Prioritize SKILL scope
        var var = vars.getVariable(name);
        if (var != null) return var;

        // Check PROFILE scope
        var playerData = getCaster().getData();
        if (playerData.isPlaying()) {
            var = playerData.getProfileSession().getVariableList().getVariable(name);
            if (var != null) return var;
        }

        // Check for PLAYER scope
        var = playerData.getVariableList().getVariable(name);
        if (var != null) return var;

        var = VariableList.SERVER.getVariable(name);
        if (var != null) return var;

        throw new VariableNotFoundException(name);
    }

    /**
     * Finds a variable with a certain name and path. There are two types
     * of variables:
     * - reserved variables, which names are reserved by MythicLib
     * to include elementary data like skill source, target location...
     * - user variables, which can be created and manipulated by the user
     * <p>
     * Variables have paths/expressions. Some examples:
     * - user_variable.subvariable1.subvariable2
     * - caster.location.x
     * - target.fire_ticks
     *
     * @param name Variable name/path (see examples above)
     * @return The (sub) variable found
     */
    @NotNull
    public Variable<?> getVariable(@NotNull String name) {

        // Find initial variable
        final var args = name.split("\\.");
        Variable<?> var;
        var i = 1;

        switch (args[0]) {

            // Access parameters
            case "modifier":
            case "parameter":
                Validate.isTrue(args.length > 1, "Please specify a modifier name");
                var = new DoubleVariable("temp", getParameter(args[i++]));
                break;

            // Skill source location
            case "source":
                var = new PositionVariable("temp", origin.clone());
                break;

            // Skill target location
            case "targetLocation":
            case "target_location":
            case "targetloc":
            case "targetl":
            case "targetLoc":
            case "target_loc":
                var = new PositionVariable("temp", getTargetLocation());
                break;

            // Skill caster
            case "caster":
                var = new PlayerVariable("temp", getCaster().getPlayer());
                break;

            // Skill caster
            case "attack":
                var = new AttackMetadataVariable("temp", getAttackSource());
                break;

            // Internal random module
            case "random":
            case "rand":
            case "rdm":
                var = RandomVariable.INSTANCE;
                break;

            // Cached stat map
            case "stat":
                var = new StatsVariable("temp", caster);
                break;

            // Skill target
            case "target":
                Validate.notNull(targetEntity, "Skill has no target");
                var = targetEntity instanceof Player ? new PlayerVariable("temp", (Player) targetEntity) : new EntityVariable("temp", targetEntity);
                break;

            // User variable (deprecated)
            case "var":
                Validate.isTrue(args.length > 1, "User variable name is not specified. Also, 'var.xxxx' notation is deprecated. Use 'xxx' directly instead");
                var = getUserVariable(args[i++]);
                break;

            // User variable
            default:
                var = getUserVariable(args[0]);
                break;
        }

        // Dives into the variable tree to find the subvariable
        for (; i < args.length; i++) {
            var = var.getVariable(args[i]);
            if (var == null) throw new VariableNotFoundException(name, args, i);
        }

        return var;
    }

    public static final Pattern INTERNAL_PLACEHOLDER_PATTERN = Pattern.compile("<([^#&|!=<>]+)>");

    @NotNull
    public String parseString(String input) {

        // Resolve internal placeholders
        final var matcher = INTERNAL_PLACEHOLDER_PATTERN.matcher(input);
        final var sb = new StringBuilder(input.length());
        while (matcher.find()) {
            final var variableName = matcher.group(1);
            final var replacement = getVariable(variableName).toString();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        input = sb.toString();

        // Parse any placeholders and apply color codes
        input = MythicLib.plugin.getPlaceholderParser().parse(getCaster().getPlayer(), input);

        return input;
    }

    //region Generators

    @NotNull
    public static SkillMetadata of(@NotNull MMOPlayerData playerData) {
        return of(playerData, EquipmentSlot.MAIN_HAND, null, null, null, null, null, null);
    }

    @NotNull
    public static SkillMetadata of(@NotNull MMOPlayerData playerData, @Nullable EquipmentSlot actionHand) {
        return of(playerData, actionHand, null, null, null, null, null, null);
    }

    @NotNull
    public static Lazy<SkillMetadata> lazyOf(@NotNull MMOPlayerData playerData) {
        return Lazy.of(() -> of(playerData));
    }

    @NotNull
    public static SkillMetadata of(@NotNull MMOPlayerData playerData, @Nullable Entity target) {
        return of(playerData, EquipmentSlot.MAIN_HAND, null, target, null, null, null, null);
    }

    @NotNull
    public static SkillMetadata of(@NotNull PlayerMetadata caster) {
        return of(caster.getData(), caster.getActionHand(), null, null, null, null, caster, null);
    }

    @NotNull
    public static SkillMetadata of(@NotNull PlayerMetadata caster, @Nullable Entity target) {
        return of(caster.getData(), caster.getActionHand(), null, target, null, null, caster, null);
    }

    @NotNull
    public static Lazy<SkillMetadata> lazyOf(@NotNull PlayerMetadata caster, @Nullable Entity target) {
        return Lazy.of(() -> of(caster, target));
    }

    @NotNull
    public static SkillMetadata of(@NotNull MMOPlayerData playerData, @Nullable Location targetLocation) {
        return of(playerData, EquipmentSlot.MAIN_HAND, null, null, targetLocation, null, null, null);
    }

    @NotNull
    public static SkillMetadata of(@NotNull MMOPlayerData playerData, @NotNull Location source, @Nullable Location targetLocation) {
        return of(playerData, EquipmentSlot.MAIN_HAND, source, null, targetLocation, null, null, null);
    }

    @NotNull
    public static SkillMetadata of(@NotNull PlayerAttackEvent attackEvent) {
        return of(attackEvent.getAttacker(), attackEvent.getEntity(), attackEvent.getAttack(), attackEvent);
    }

    @NotNull
    public static Lazy<SkillMetadata> lazyOf(@NotNull PlayerAttackEvent attackEvent) {
        return Lazy.of(() -> of(attackEvent));
    }

    @NotNull
    public static SkillMetadata of(@NotNull PlayerMetadata caster, @Nullable Entity target, @Nullable AttackMetadata attack,
                                   @Nullable Event sourceEvent) {
        return of(caster.getData(), caster.getActionHand(), null, target, null, attack, caster, sourceEvent);
    }

    @NotNull
    public static SkillMetadata of(@NotNull MMOPlayerData playerData, @Nullable EquipmentSlot actionHand,
                                   @Nullable Location source, @Nullable Entity target, @Nullable Location targetLocation,
                                   @Nullable AttackMetadata attack, @Nullable PlayerMetadata caster, @Nullable Event sourceEvent) {
        return new SkillMetadata(
                null,
                Objects.requireNonNullElseGet(caster, () -> playerData.getStatMap().cache(Objects.requireNonNullElse(actionHand, EquipmentSlot.MAIN_HAND))),
                new VariableList(VariableScope.SKILL),
                Objects.requireNonNullElseGet(source, () -> playerData.getPlayer().getLocation()),
                targetLocation,
                target,
                null,
                attack,
                sourceEvent);
    }

    //endregion

    //region Deprecated

    /**
     * @deprecated Use {@link PlayerMetadata#attack(LivingEntity, double, List)} instead
     */
    @NotNull
    @Deprecated
    public AttackMetadata attack(@NotNull LivingEntity target, double damage, DamageType... types) {
        return caster.attack(target, damage, Arrays.asList(types));
    }

    /**
     * @see #hasAttackSource()
     * @deprecated
     */
    @Deprecated
    public boolean hasAttackBound() {
        return hasAttackSource();
    }

    /**
     * Looks into the target entity metadata for an AttackMetadata.
     * If it finds one then it has to be from the skill caster.
     *
     * @return Eventual attack currently being dealt to the entity.
     * @see #getAttackSource()
     * @deprecated
     */
    @Deprecated
    public AttackMetadata getAttack() {
        return getAttackSource();
    }

    @Override
    public String toString() {
        return "SkillMetadata{" +
                "attackSource=" + attackSource +
                ", sourceEvent=" + sourceEvent +
                ", targetLocation=" + targetLocation +
                ", targetEntity=" + targetEntity +
                ", origin=" + origin +
                ", caster=" + caster +
                ", cast=" + cast +
                ", vars=" + vars +
                '}';
    }

    /**
     * @see #getVariable(String)
     * @deprecated References no longer exist, in order to reduce confusion, MythicLib
     *         now reserves specific names for internal variables, see {@link #RESERVED_VARIABLE_NAMES}
     */
    @Deprecated
    public Variable getReference(String name) {
        return getVariable(name);
    }

    /**
     * @see #getVariable(String)
     * @deprecated There are no longer major differences between internal/reserved variables
     *         and user variables, so this method is no longer relevant.
     */
    @Deprecated
    public Variable getCustomVariable(String name) {
        return getUserVariable(name);
    }

    /**
     * @deprecated Skill modifiers are now called "parameters"
     */
    @Deprecated
    public double getModifier(String param) {
        return getParameter(param);
    }

    @Deprecated
    public SkillMetadata(Skill cast, @NotNull AttackMetadata attackMeta, @NotNull Location origin, @Nullable Location targetLocation, @Nullable Entity targetEntity) {
        this(cast, (PlayerMetadata) attackMeta.getAttacker(), new VariableList(VariableScope.SKILL), origin, targetLocation, targetEntity, null, attackMeta, null);
    }

    @Deprecated
    public SkillMetadata(Skill cast, @NotNull PlayerMetadata caster, @NotNull VariableList vars, @Nullable AttackMetadata attackMeta, @NotNull Location origin, @Nullable Location targetLocation, @Nullable Entity targetEntity, @Nullable SkillOrientation orientation) {
        this(cast, caster, vars, origin, targetLocation, targetEntity, orientation, attackMeta, null);
    }

    @Deprecated
    public SkillMetadata(Skill cast, @NotNull MMOPlayerData caster) {
        this(cast, caster.getStatMap().cache(EquipmentSlot.MAIN_HAND), new VariableList(VariableScope.SKILL), caster.getPlayer().getLocation(), null, null, null, null, null);
    }

    @Deprecated
    public SkillMetadata(Skill cast, @NotNull Location origin, @Nullable Location targetLocation, @Nullable Entity targetEntity, @NotNull AttackMetadata attackMeta) {
        this(cast, (PlayerMetadata) attackMeta.getAttacker(), new VariableList(VariableScope.SKILL), origin, targetLocation, targetEntity, null, attackMeta, null);
    }

    @Deprecated
    public SkillMetadata(Skill cast, @NotNull PlayerMetadata caster, @NotNull VariableList vars, @NotNull Location origin, @Nullable Location targetLocation, @Nullable Entity targetEntity, @Nullable SkillOrientation orientation) {
        this(cast, caster, vars, origin, targetLocation, targetEntity, orientation, null, null);
    }

    //endregion
}
