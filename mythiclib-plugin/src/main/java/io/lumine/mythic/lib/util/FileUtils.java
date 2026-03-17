package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public class FileUtils {

    public static <T> void iterateConfigSectionList(@NotNull ConfigurationSection config,
                                                    @NotNull List<T> list,
                                                    @NotNull Function<ConfigurationSection, T> subconfigHandler,
                                                    @NotNull Function<Integer, T> fill,
                                                    @NotNull BiConsumer<String, RuntimeException> errorHandler) {
        int expectedOrdinal = 1;

        for (String key : config.getKeys(false))
            try {
                final int index = Integer.parseInt(key);
                final ConfigurationSection subconfig = config.getConfigurationSection(key);
                Validate.notNull(subconfig, "Not a configuration section");

                // Replace
                if (index < expectedOrdinal) list.set(index, subconfigHandler.apply(subconfig));
                else {
                    while (expectedOrdinal < index)
                        list.add(fill.apply(expectedOrdinal++));
                    list.add(subconfigHandler.apply(subconfig));
                    expectedOrdinal++;
                }

            } catch (RuntimeException exception) {
                errorHandler.accept(key, exception);
            }
    }

    public static void loadSingleObjectsFromFolder(@NotNull Plugin plugin,
                                                   @NotNull String path,
                                                   @NotNull BiConsumer<String, ConfigurationSection> action,
                                                   @NotNull String errorMessageFormat) {

        // Action to perform
        final Consumer<File> fileAction = file -> {
            final var config = YamlConfiguration.loadConfiguration(file);
            try {
                final String name = file.getName().substring(0, file.getName().length() - 4);
                action.accept(name, config);
            } catch (Exception throwable) {
                plugin.getLogger().log(Level.WARNING, String.format(errorMessageFormat, file.getName(), throwable.getMessage()));
            }
        };

        // Perform on all paths
        exploreFolderRecursively(getFile(plugin, path), fileAction);
    }

    public static void loadObjectsFromFolder(@NotNull Plugin plugin,
                                             @NotNull String path,
                                             @NotNull BiConsumer<String, ConfigurationSection> action,
                                             @NotNull String errorMessageFormat) {

        // Action to perform
        final Consumer<File> fileAction = file -> {
            final var config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false))
                try {
                    action.accept(key, config.getConfigurationSection(key));
                } catch (Exception throwable) {
                    plugin.getLogger().log(Level.WARNING, String.format(errorMessageFormat, key, file.getName(), throwable.getMessage()));
                }
        };

        // Perform on all paths
        exploreFolderRecursively(getFile(plugin, path), fileAction);
    }

    public static void loadRawObjectsFromFolder(@NotNull Plugin plugin,
                                                @NotNull String path,
                                                @NotNull Consumer<File> action,
                                                @NotNull String errorMessageFormat) {

        // Action to perform
        final Consumer<File> fileAction = file -> {
            try {
                action.accept(file);
            } catch (Exception throwable) {
                plugin.getLogger().log(Level.WARNING, String.format(errorMessageFormat, file.getName(), throwable.getMessage()));
            }
        };

        // Perform on all paths
        exploreFolderRecursively(getFile(plugin, path), fileAction);
    }

    public static void exploreFolderRecursively(@Nullable File file, @NotNull Consumer<File> action) {
        if (file == null || !file.exists()) return;

        if (file.isFile()) action.accept(file);
        else Arrays.stream(file.listFiles()).sorted().forEach(subfile -> exploreFolderRecursively(subfile, action));
    }

    @NotNull
    public static File getFile(@NotNull Plugin plugin, @NotNull String path) {
        return new File(plugin.getDataFolder() + "/" + path);
    }

    public static boolean moveIfExists(@NotNull Plugin plugin,
                                       @NotNull String filePath,
                                       @NotNull String newFolderPath) {
        final File existing = getFile(plugin, filePath);
        final boolean result = existing.exists();
        if (result) {
            final String fullPath = newFolderPath + "/" + filePath;
            mkdirFolders(plugin, fullPath);
            Validate.isTrue(existing.renameTo(getFile(plugin, fullPath)), "Could not move '" + filePath + "' to '" + newFolderPath + "'");
        }
        return result;
    }

    private static void mkdirFolders(@NotNull Plugin plugin, @NotNull String fullPath) {
        var currentPath = "";
        final var subpaths = fullPath.split("/");
        for (int i = 0; i < subpaths.length - 1; i++) {
            currentPath += "/" + subpaths[i];
            getFile(plugin, currentPath).mkdir();
        }
    }

    public static void copyDefaultFile(@NotNull Plugin plugin, @NotNull String path) {
        mkdirFolders(plugin, path);

        final var file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) try {
            Files.copy(plugin.getResource("default/" + path), file.getAbsoluteFile().toPath());
        } catch (Exception throwable) {
            throw new RuntimeException("Could not load default file '" + path + "'", throwable);
        }
    }

    @Deprecated
    public static void loadObjectsFromFolder(@NotNull Plugin plugin,
                                             @NotNull String path,
                                             boolean singleObject,
                                             @NotNull BiConsumer<String, ConfigurationSection> action,
                                             @NotNull String errorMessageFormat) {
        if (singleObject) loadSingleObjectsFromFolder(plugin, path, action, errorMessageFormat);
        else loadObjectsFromFolder(plugin, path, action, errorMessageFormat);
    }
}
