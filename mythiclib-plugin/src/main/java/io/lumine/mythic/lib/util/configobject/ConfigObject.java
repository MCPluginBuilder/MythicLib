package io.lumine.mythic.lib.util.configobject;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * An interface to cover both configuration sections and line configs.
 * <p>
 * That way there are two formats for creating a skill, either using
 * configuration sections (pro is that it takes more space and looks
 * less crammed, more familiar with Fabled) or line configs (for
 * users more familiar with MM)
 * <p>
 * This is also used to wrap mutable objects and only make public
 * read-only methods.
 * <p>
 * There are always two methods to get a primitive with some key,
 * one method with a default value and another method which throws
 * a NPE if the returned object is null. The ..OrDefault method always
 * takes one extra map checkup so it's best to use it on startup only.
 *
 * @author jules
 */
public interface ConfigObject {

    //region Getters

    @NotNull String getString(@NotNull String key);

    @Nullable String getString(@NotNull String key, @Nullable String defaultValue);

    double getDouble(@NotNull String key);

    double getDouble(@NotNull String key, double defaultValue);

    int getInt(@NotNull String key);

    int getInt(@NotNull String key, int defaultValue);

    boolean getBoolean(@NotNull String key);

    boolean getBoolean(@NotNull String key, boolean defaultValue);

    float getFloat(@NotNull String key);

    float getFloat(@NotNull String key, float defaultValue);

    //endregion

    //region Finders

    String string(@NotNull String... aliases);

    String stringFb(@Nullable String defaultValue, @NotNull String... aliases);

    double dble(@NotNull String... aliases);

    double dble(double defaultValue, @NotNull String... aliases);

    float flpt(@NotNull String... aliases);

    float flpt(float defaultValue, @NotNull String... aliases);

    int integer(@NotNull String... aliases);

    int integer(int defaultValue, @NotNull String... aliases);

    boolean bool(@NotNull String... aliases);

    boolean bool(boolean defaultValue, @NotNull String... aliases);

    //endregion

    @NotNull
    default Script script(@NotNull String... aliases) {
        for (String key : aliases) if (contains(key)) return getScript(key);
        throw new MissingArgumentException(aliases);
    }

    @Contract("!null, _ -> !null")
    @Nullable
    default Script script(@Nullable Script defaultValue, @NotNull String... aliases) {
        for (String key : aliases) if (contains(key)) return getScript(key);
        return defaultValue;
    }

    @Contract("!null, _, _ -> !null")
    @Nullable
    default <T> T parse(@Nullable T defaultValue, @NotNull Function<String, T> parser, @NotNull String... aliases) {
        for (var key : aliases) if (contains(key)) return parser.apply(getString(key));
        return defaultValue;
    }

    @NotNull
    default <T> T parse(@NotNull Function<String, T> parser, @NotNull String... aliases) {
        for (var key : aliases) if (contains(key)) return parser.apply(getString(key));
        throw new MissingArgumentException(aliases);
    }

    @NotNull
    default NumericExpression numericExpr(@NotNull String... aliases) {
        for (var key : aliases) if (contains(key)) return NumericExpression.compile(getString(key));
        throw new MissingArgumentException(aliases);
    }

    @Contract("!null, _ -> !null")
    @Nullable
    default NumericExpression numericExpr(@Nullable NumericExpression defaultValue, @NotNull String... aliases) {
        for (String key : aliases) if (contains(key)) return NumericExpression.compile(getString(key));
        return defaultValue;
    }

    @Nullable
    default Script getScriptOrNull(String key) {
        return contains(key) ? MythicLib.plugin.getSkills().getScriptOrThrow(getString(key)) : null;
    }

    @NotNull
    default Script getScript(String... aliases) {
        return MythicLib.plugin.getSkills().getScriptOrThrow(string(aliases));
    }

    @NotNull
    default EntityTargeter getEntityTargeter(String key) {
        return MythicLib.plugin.getSkills().loadEntityTargeter(adaptObject(key));
    }

    @NotNull
    default LocationTargeter getLocationTargeter(String key) {
        return MythicLib.plugin.getSkills().loadLocationTargeter(adaptObject(key));
    }

    @NotNull ConfigObject getObject(String key);

    /**
     * This either retrieves the object with given key if it exists,
     * or if the given key is associated to a string, encapsulates this
     * string value into a new object and sets the `type` key of that new
     * object to the retrieved string value.
     * <p>
     * This is primarily used for targeters and condition shortcuts.
     */
    @NotNull
    ConfigObject adaptObject(String key);

    boolean contains(String key);

    @NotNull Set<String> getKeys();

    @Nullable String getKey();

    default boolean hasKey() {
        return getKey() != null;
    }

    //region Deprecated

    /**
     * @see #numericExpr(NumericExpression, String...)
     * @deprecated
     */
    @Deprecated
    default DoubleFormula getDoubleFormula(String key, DoubleFormula defaultValue) {
        return contains(key) ? getDoubleFormula(key) : Objects.requireNonNull(defaultValue, "Default value cannot be null");
    }

    /**
     * Throws an IAE if any of the given key
     * is not found in the config object
     *
     * @see #flpt(String...)
     * @see #bool(String...)
     * @see #string(String...)
     * @see #integer(String...)
     * @see #dble(String...)
     * @deprecated
     */
    @Deprecated
    default void validateKeys(String... keys) {
        for (String key : keys)
            Validate.isTrue(contains(key), "Could not find key '" + key + "' in config");
    }

    /**
     * Throws IAE if the config has less than X parameters
     *
     * @param count The amount of arguments
     * @deprecated Use finders instead
     */
    @Deprecated
    default void validateArgs(int count) {
        Validate.isTrue(getKeys().size() >= count, "Config must have at least " + count + " parameters");
    }

    @NotNull
    @Deprecated
    default DoubleFormula getDoubleFormula(@NotNull String... aliases) {
        for (var key : aliases) if (contains(key)) return new DoubleFormula(getString(key));
        throw new MissingArgumentException(aliases);
    }

    @Contract("!null, _ -> !null")
    @Nullable
    @Deprecated
    default DoubleFormula getDoubleFormula(@Nullable DoubleFormula defaultValue, @NotNull String... aliases) {
        for (String key : aliases) if (contains(key)) return new DoubleFormula(getString(key));
        return defaultValue;
    }

    //endregion
}
