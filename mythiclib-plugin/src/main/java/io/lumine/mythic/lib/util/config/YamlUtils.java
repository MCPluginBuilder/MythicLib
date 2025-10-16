package io.lumine.mythic.lib.util.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class YamlUtils {

    @Nullable
    public static String getString(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates) {
            var found = config.getString(candidate);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * @see #getInteger(ConfigurationSection, String...)
     */
    public static int getInt(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getInt(candidate);
        return 0;
    }

    /**
     * @see #getInt(ConfigurationSection, String...)
     */
    @Nullable
    public static Integer getInteger(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getInt(candidate);
        return null;
    }

    /**
     * @see #getBooleanObj(ConfigurationSection, String...)
     */
    public static boolean getBoolean(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getBoolean(candidate);
        return false;
    }

    /**
     * @see #getBoolean(ConfigurationSection, String...)
     */
    @Nullable
    public static Boolean getBooleanObj(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getBoolean(candidate);
        return null;
    }

    /**
     * @see #getBoolean(ConfigurationSection, String...)
     * @deprecated
     */
    @Deprecated
    public static boolean getBool(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        return getBoolean(config, candidates);
    }

    /**
     * @see #getFloatObj(ConfigurationSection, String...)
     */
    public static float getFloat(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return (float) config.getDouble(candidate);
        return 0;
    }

    /**
     * @see #getFloat(ConfigurationSection, String...)
     */
    @Nullable
    public static Float getFloatObj(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return (float) config.getDouble(candidate);
        return null;
    }

    /**
     * @see #getDoubleObj(ConfigurationSection, String...)
     */
    public static double getDouble(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getDouble(candidate);
        return 0d;
    }

    /**
     * @see #getDouble(ConfigurationSection, String...)
     */
    @Nullable
    public static Double getDoubleObj(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getDouble(candidate);
        return null;
    }

    @Nullable
    public static List<String> getStringList(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates) {
            var found = config.getStringList(candidate);
            if (!found.isEmpty()) return found;
        }
        return null;
    }

    @Nullable
    public static Object get(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates) {
            var found = config.get(candidate);
            if (found != null) return found;
        }
        return null;
    }

    public static <T> boolean containsOneKey(@NotNull ConfigurationSection config, @NotNull Iterable<T> values) {
        for (var value : values)
            if (config.contains(value.toString())) return true;
        return false;
    }

    public static <T extends Enum<?>> boolean containsOneKey(@NotNull ConfigurationSection config, @NotNull T[] enumValues, @NotNull Function<T, String> name) {
        for (var value : enumValues)
            if (config.contains(name.apply(value))) return true;
        return false;
    }
}
