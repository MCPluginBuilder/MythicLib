package io.lumine.mythic.lib.util.configobject;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class EmptyConfigObject implements ConfigObject {

    @Override
    public String getString(String key) {
        throw new NullPointerException("Could not find string with key '" + key + "'");
    }

    @Override
    public String getString(String key, String defaultValue) {
        return defaultValue;
    }

    @Override
    public double getDouble(String key) {
        throw new NullPointerException("Could not find double with key '" + key + "'");
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return defaultValue;
    }

    @Override
    public int getInt(String key) {
        throw new NullPointerException("Could not find int with key '" + key + "'");
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return defaultValue;
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return defaultValue;
    }

    //region Modern

    @Override
    public @NotNull Optional<Double> dble(String... aliases) {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<Float> flpt(String... aliases) {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<Integer> integer(String... aliases) {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<String> string(String... aliases) {
        return Optional.empty();
    }

    //endregion

    @Override
    public float getFloat(String key) {
        throw new NullPointerException("Could not find float with key '" + key + "'");
    }

    @NotNull
    @Override
    public ConfigObject adaptObject(String key) {
        throw new NullPointerException("Could not find entity targeter with key '" + key + "'");
    }

    @Override
    public boolean getBoolean(String key) {
        throw new NullPointerException("Could not find boolean with key '" + key + "'");
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return defaultValue;
    }

    @NotNull
    @Override
    public ConfigObject getObject(String key) {
        throw new NullPointerException("Could not find object with key '" + key + "'");
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public @NotNull Set<String> getKeys() {
        return new HashSet<>();
    }

    @Override
    public String getKey() {
        return null;
    }
}
