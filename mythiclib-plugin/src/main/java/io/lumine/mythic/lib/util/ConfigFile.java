package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @see io.lumine.mythic.lib.util.config.YamlFile
 * @deprecated
 */
@Deprecated
public class ConfigFile {
    private final File file;
    private final String name;
    private final FileConfiguration config;

    @Deprecated
    public ConfigFile(String name) {
        this(MythicLib.plugin, "", name);
    }

    @Deprecated
    public ConfigFile(String folder, String name) {
        this(MythicLib.plugin, folder, name);
    }

    @Deprecated
    public ConfigFile(Plugin plugin, String name) {
        this(plugin, "", name);
    }

    @Deprecated
    public ConfigFile(Plugin plugin, String folder, String name) {
        config = YamlConfiguration.loadConfiguration(file = new File(plugin.getDataFolder() + folder, (this.name = name) + ".yml"));
    }

    @Deprecated
    public boolean exists() {
        return file.exists();
    }

    @Deprecated
    public FileConfiguration getConfig() {
        return config;
    }

    @Deprecated
    public void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            MythicLib.plugin.getLogger().log(Level.SEVERE, "Could not save " + name + ".yml: " + exception.getMessage());
        }
    }

    @Deprecated
    public void delete() {
        if (file.exists() && !file.delete())
            MythicLib.plugin.getLogger().log(Level.SEVERE, "Could not delete " + name + ".yml.");
    }
}