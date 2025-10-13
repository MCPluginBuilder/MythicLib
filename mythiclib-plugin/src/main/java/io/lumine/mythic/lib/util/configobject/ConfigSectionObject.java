package io.lumine.mythic.lib.util.configobject;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.Script;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ConfigSectionObject implements ConfigObject {
    private final ConfigurationSection config;

    public ConfigSectionObject(ConfigurationSection config) {
        this.config = config;
    }

    @Override
    public String getString(String key) {
        return Objects.requireNonNull(config.getString(key), "Could not find string with key '" + key + "'");
    }

    @Override
    public String getString(String key, String defaultValue) {
        return config.getString(key, Objects.requireNonNull(defaultValue, "Default value cannot be null"));
    }

    @Override
    public double getDouble(String key) {
        return config.getDouble(key);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return config.getDouble(key, defaultValue);
    }

    @Override
    public float getFloat(String key) {
        return (float) config.getDouble(key);
    }

    //region Modern

    @Override
    public float getFloat(String key, float defaultValue) {
        return (float) config.getDouble(key, defaultValue);
    }

    @Override
    public @NotNull Optional<Float> flpt(String... aliases) {
        for (var alias : aliases)
            if (contains(alias)) return Optional.of((float) config.getDouble(alias));
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<String> string(String... aliases) {
        for (var alias : aliases)
            if (contains(alias)) return Optional.of(config.getString(alias));
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<Integer> integer(String... aliases) {
        for (var alias : aliases)
            if (contains(alias)) return Optional.of(config.getInt(alias));
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<Double> dble(String... aliases) {
        for (var alias : aliases)
            if (contains(alias)) return Optional.of(config.getDouble(alias));
        return Optional.empty();
    }

    //endregion

    @Override
    public int getInt(String key) {
        return config.getInt(key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

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
