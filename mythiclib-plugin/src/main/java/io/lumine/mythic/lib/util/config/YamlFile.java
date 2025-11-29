package io.lumine.mythic.lib.util.config;

import io.lumine.mythic.lib.MythicLib;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class YamlFile extends ConfigFile<FileConfiguration> {
    public YamlFile(String fileName) {
        this(MythicLib.plugin, null, fileName);
    }

    public YamlFile(@Nullable String folderPath, @NotNull String fileName) {
        this(MythicLib.plugin, folderPath, fileName, true);
    }

    public YamlFile(@NotNull Plugin plugin, @NotNull String fileName) {
        this(plugin, null, fileName, true);
    }

    public YamlFile(@NotNull Plugin plugin, @Nullable String folderPath, @NotNull String fileName) {
        this(plugin, folderPath, fileName, true);
    }

    public YamlFile(@NotNull Plugin plugin, @Nullable String folderPath, @NotNull String fileName, boolean read) {
        super(plugin, folderPath, fileName + ".yml");

        setContent(read ? YamlConfiguration.loadConfiguration(getFile()) : new YamlConfiguration());
    }

    public YamlFile(@NotNull Plugin plugin, @Nullable String folderPath, @NotNull String fileName, @NotNull FileConfiguration content) {
        super(plugin, folderPath, fileName + ".yml");

        setContent(content);
    }

    @Override
    public void save() {
        try {
            getContent().save(getFile());
        } catch (IOException exception) {
            MythicLib.plugin.getLogger().log(Level.SEVERE, "Could not save YAML file '" + getFile().getName() + "': " + exception.getMessage());
        }
    }

    @NotNull
    public static YamlFile fromJarFile(@NotNull Plugin plugin, @Nullable String folderPath, @NotNull String filePath) {
        String fullFilePath = ((folderPath != null && !folderPath.isEmpty()) ? folderPath + "/" + filePath : filePath) + ".yml";
        try (var in = plugin.getResource(fullFilePath)) {
            if (in == null) throw new IllegalArgumentException("Internal resource not found in JAR: " + filePath);

            var config = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            return new YamlFile(plugin, folderPath, filePath, config);

        } catch (IOException exception) {
            throw new RuntimeException("Could not load internal YAML '" + filePath + "'", exception);
        }
    }
}
