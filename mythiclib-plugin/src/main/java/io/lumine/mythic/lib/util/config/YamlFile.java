package io.lumine.mythic.lib.util.config;

import io.lumine.mythic.lib.MythicLib;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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

    @Override
    public void save() {
        try {
            getContent().save(getFile());
        } catch (IOException exception) {
            MythicLib.plugin.getLogger().log(Level.SEVERE, "Could not save YAML file '" + getFile().getName() + "': " + exception.getMessage());
        }
    }
}
