package io.lumine.mythic.lib.util.config;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

public abstract class ConfigFile<T> {
    private final Plugin plugin;
    private final File file;

    private T content;

    public ConfigFile(@NotNull Plugin plugin, @Nullable String folderPath, @NotNull String fileName) {
        this.plugin = plugin;
        this.file = new File(resolveFilePath(plugin, folderPath, fileName));
    }

    @NotNull
    public T getContent() {
        return content;
    }

    public boolean hasContent() {
        return content != null;
    }

    public void setContent(T t) {
        this.content = Objects.requireNonNull(t, "Content cannot be null");
    }

    public boolean exists() {
        return file.exists();
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    public File getFile() {
        return file;
    }

    public void delete() {
        if (file.exists() && !file.delete())
            MythicLib.plugin.getLogger().log(Level.SEVERE, "Could not delete " + getClass().getSimpleName() + " '" + file.getName() + "'");
    }

    public abstract void save();

    @NotNull
    private static String resolveFilePath(@NotNull Plugin plugin, @Nullable String folderPath, @NotNull String fileName) {
        var builder = new StringBuilder();

        // Plugin data folder
        Validate.notNull(fileName, "Plugin cannot be null");
        builder.append(plugin.getDataFolder());
        // Folder path

        if (folderPath != null && !folderPath.isEmpty()) builder.append("/").append(folderPath);

        // File name
        Validate.notNull(fileName, "File name cannot be null");
        builder.append("/").append(fileName);

        return builder.toString();
    }
}
