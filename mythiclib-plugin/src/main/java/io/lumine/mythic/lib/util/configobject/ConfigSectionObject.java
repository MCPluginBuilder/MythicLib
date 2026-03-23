package io.lumine.mythic.lib.util.configobject;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.Script;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class ConfigSectionObject implements ConfigObject {
    private final ConfigurationSection config;

    public ConfigSectionObject(ConfigurationSection config) {
        this.config = config;
    }

    //region Getters

    @Override
    public @NotNull String getString(@NotNull String key) {
        final var obj = config.get(key);
        if (obj == null) throw new MissingArgumentException(key);
        return String.valueOf(obj);
    }

    @Override
    public @Nullable String getString(@NotNull String key, @Nullable String defaultValue) {
        return config.getString(key, defaultValue);
    }

    @Override
    public double getDouble(@NotNull String key) {
        if (!config.contains(key)) throw new MissingArgumentException(key);
        return config.getDouble(key);
    }

    @Override
    public double getDouble(@NotNull String key, double defaultValue) {
        return config.getDouble(key, defaultValue);
    }

    @Override
    public float getFloat(@NotNull String key) {
        if (!config.contains(key)) throw new MissingArgumentException(key);
        return (float) config.getDouble(key);
    }

    @Override
    public float getFloat(@NotNull String key, float defaultValue) {
        return (float) config.getDouble(key, defaultValue);
    }

    @Override
    public int getInt(@NotNull String key) {
        if (!config.contains(key)) throw new MissingArgumentException(key);
        return config.getInt(key);
    }

    @Override
    public int getInt(@NotNull String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }

    @Override
    public boolean getBoolean(@NotNull String key) {
        if (!config.contains(key)) throw new MissingArgumentException(key);
        return config.getBoolean(key);
    }

    @Override
    public boolean getBoolean(@NotNull String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    //endregion

    //region Finders

    @Override
    public String string(@NotNull String... aliases) {
        for (var alias : aliases)
            if (config.contains(alias)) return config.getString(alias);
        throw new MissingArgumentException(aliases);
    }

    @Override
    public String stringFb(@NotNull String defaultValue, @NotNull String... aliases) {
        for (var alias : aliases)
            if (config.contains(alias)) return config.getString(alias);
        return Objects.requireNonNull(defaultValue);
    }

    @Override
    public double dble(@NotNull String... aliases) {
        for (var alias : aliases)
            if (config.contains(alias)) return config.getDouble(alias);
        throw new MissingArgumentException(aliases);
    }

    @Override
    public double dble(double defaultValue, @NotNull String... aliases) {
        for (var alias : aliases)
            if (config.contains(alias)) return config.getDouble(alias);
        return defaultValue;
    }

    @Override
    public int integer(@NotNull String... aliases) {
        for (var alias : aliases)
            if (config.contains(alias)) return config.getInt(alias);
        throw new MissingArgumentException(aliases);
    }

    @Override
    public int integer(int defaultValue, @NotNull String... aliases) {
        for (var alias : aliases)
            if (config.contains(alias)) return getInt(alias);
        return defaultValue;
    }

    @Override
    public float flpt(@NotNull String... aliases) {
        for (var alias : aliases)
            if (config.contains(alias)) return (float) config.getDouble(alias);
        throw new MissingArgumentException(aliases);
    }

    @Override
    public float flpt(float defaultValue, @NotNull String... aliases) {
        for (var alias : aliases)
            if (config.contains(alias)) return (float) config.getDouble(alias);
        return defaultValue;
    }

    @Override
    public boolean bool(@NotNull String... aliases) {
        return false;
    }

    @Override
    public boolean bool(boolean defaultValue, @NotNull String... aliases) {
        for (var alias : aliases)
            if (config.contains(alias)) return config.getBoolean(alias);
        return defaultValue;
    }

    //endregion

    @Nullable
    public Script getScriptOrNull(String key) {
        return contains(key) ? getScript(key) : null;
    }

    @NotNull
    public Script getScript(String key) {
        return MythicLib.plugin.getSkills().loadScript(key, config.get(key));
    }

    @NotNull
    @Override
    public ConfigSectionObject getObject(String key) {
        return new ConfigSectionObject(Objects.requireNonNull(config.getConfigurationSection(key), "Could not find section with key '" + key + "'"));
    }

    @NotNull
    @Override
    public ConfigSectionObject adaptObject(String key) {
        final Object found = config.get(key);
        if (found == null) throw new MissingArgumentException(key);

        final ConfigurationSection loadFrom;
        if (found instanceof ConfigurationSection) loadFrom = (ConfigurationSection) found;
        else if (found instanceof String) {
            loadFrom = new MemoryConfiguration();
            loadFrom.set("type", found);
        } else throw new IllegalArgumentException("Expecting either a string or object");

        return new ConfigSectionObject(loadFrom);
    }

    @Override
    public boolean contains(String key) {
        return config.contains(key);
    }

    @NotNull
    @Override
    public Set<String> getKeys() {
        return config.getKeys(false);
    }

    @Override
    public String getKey() {
        return config.getName();
    }
}
