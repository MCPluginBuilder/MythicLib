package io.lumine.mythic.lib.util.configobject;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * An interface to cover both configuration sections and line configs.
 * <p>
 * That way there are two formats for creating a skill, either using
 * configuration sections (pro is that it takes more space and looks
 * less crammed, more familiar with Fabled) or line configs (for
 * users more familiar with MM)
 * <p>
 * This is also used to restrain the methods from the ConfigurationSection
 * class which include editing methods. Also provides util methods
 * like {@link #validateKeys(String...)} which aren't present in
 * bukkit config sections
 * <p>
 * There are always two methods to get a primitive with some key,
 * one method with a default value and another method which throws
 * a NPE if the returned object is null. The ..OrDefault method always
 * takes one extra map checkup so it's best to use it on startup only.
 *
 * @author jules
 */
public interface ConfigObject {
    String getString(String key);

    String getString(String key, String defaultValue);

    double getDouble(String key);

    double getDouble(String key, double defaultValue);

    int getInt(String key);

    int getInt(String key, int defaultValue);

    boolean getBoolean(String key);

    boolean getBoolean(String key, boolean defaultValue);

    default DoubleFormula getDoubleFormula(String key) {
        return new DoubleFormula(getString(key));
    }

    default DoubleFormula getDoubleFormula(String key, DoubleFormula defaultValue) {
        return contains(key) ? getDoubleFormula(key) : Objects.requireNonNull(defaultValue, "Default value cannot be null");
    }

    @Nullable
    default Script getScriptOrNull(String key) {
        return contains(key) ? MythicLib.plugin.getSkills().getScriptOrThrow(getString(key)) : null;
    }

    @NotNull
    default Script getScript(String key) {
        return MythicLib.plugin.getSkills().getScriptOrThrow(getString(key));
    }

    @NotNull
    default EntityTargeter getEntityTargeter(String key) {
        return MythicLib.plugin.getSkills().loadEntityTargeter(adaptObject(key));
    }

    @NotNull
    default LocationTargeter getLocationTargeter(String key) {
        return MythicLib.plugin.getSkills().loadLocationTargeter(adaptObject(key));
    }

    @NotNull
    ConfigObject getObject(String key);

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

    /**
     * Throws an IAE if any of the given key
     * is not found in the config object
     */
    default void validateKeys(String... keys) {
        for (String key : keys)
            Validate.isTrue(contains(key), "Could not find key '" + key + "' in config");
    }

    /**
     * Throws IAE if the config has less than X parameters
     *
     * @param count The amount of arguments
     */
    default void validateArgs(int count) {
        Validate.isTrue(getKeys().size() >= count, "Config must have at least " + count + " parameters");
    }
}
