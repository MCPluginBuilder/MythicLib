package io.lumine.mythic.lib.util.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class YamlUtils {

    @Nullable
    public static String getString(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates) {
            var found = config.getString(candidate);
            if (found != null) return found;
        }
        return null;
    }

    @Nullable
    public static Integer getInteger(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getInt(candidate);
        return null;
    }

    public static int getInt(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getInt(candidate);
        return 0;
    }

    @Nullable
    public static Boolean getBoolean(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getBoolean(candidate);
        return null;
    }

    public static boolean getBool(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getBoolean(candidate);
        return false;
    }

    @Nullable
    public static Float getFloat(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return (float) config.getDouble(candidate);
        return null;
    }

    public static double getDouble(@NotNull ConfigurationSection config, @NotNull String... candidates) {
        for (var candidate : candidates)
            if (config.contains(candidate)) return config.getDouble(candidate);
        return 0;
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
}
