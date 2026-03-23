package io.lumine.mythic.lib.util.configobject;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class EmptyConfigObject implements ConfigObject {

    //region Getters

    @Override
    public @NotNull String getString(@NotNull String key) {
        throw new MissingArgumentException(key);
    }

    @Override
    @Contract("_, !null -> !null")
    public @Nullable String getString(@NotNull String key, @Nullable String defaultValue) {
        return defaultValue;
    }

    @Override
    public double getDouble(@NotNull String key) {
        throw new MissingArgumentException(key);
    }

    @Override
    public double getDouble(@NotNull String key, double defaultValue) {
        return defaultValue;
    }

    @Override
    public int getInt(@NotNull String key) {
        throw new MissingArgumentException(key);
    }

    @Override
    public int getInt(@NotNull String key, int defaultValue) {
        return defaultValue;
    }

    @Override
    public float getFloat(@NotNull String key) {
        throw new MissingArgumentException(key);
    }

    @Override
    public float getFloat(@NotNull String key, float defaultValue) {
        return defaultValue;
    }

    @Override
    public boolean getBoolean(@NotNull String key) {
        throw new MissingArgumentException(key);
    }

    @Override
    public boolean getBoolean(@NotNull String key, boolean defaultValue) {
        return defaultValue;
    }

    //endregion

    //region Finders


    @Override
    public String string(@NotNull String... aliases) {
        throw new MissingArgumentException(aliases);
    }

    @Override
    public String stringFb(@NotNull String defaultValue, @NotNull String... aliases) {
        return Objects.requireNonNull(defaultValue);
    }

    @Override
    public double dble(@NotNull String... aliases) {
        throw new MissingArgumentException(aliases);
    }

    @Override
    public double dble(double defaultValue, @NotNull String... aliases) {
        return defaultValue;
    }

    @Override
    public int integer(@NotNull String... aliases) {
        throw new MissingArgumentException(aliases);
    }

    @Override
    public int integer(int defaultValue, @NotNull String... aliases) {
        return defaultValue;
    }

    @Override
    public float flpt(@NotNull String... aliases) {
        throw new MissingArgumentException(aliases);
    }

    @Override
    public float flpt(float defaultValue, @NotNull String... aliases) {
        return defaultValue;
    }

    @Override
    public boolean bool(@NotNull String... aliases) {
        throw new MissingArgumentException(aliases);
    }

    @Override
    public boolean bool(boolean defaultValue, @NotNull String... aliases) {
        return defaultValue;
    }

    //endregion

    @NotNull
    @Override
    public ConfigObject adaptObject(String key) {
        throw new MissingArgumentException(key);
    }

    @NotNull
    @Override
    public ConfigObject getObject(String key) {
        throw new MissingArgumentException(key);
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public @NotNull Set<String> getKeys() {
        return Set.of();
    }

    @Override
    public String getKey() {
        return null;
    }
}
