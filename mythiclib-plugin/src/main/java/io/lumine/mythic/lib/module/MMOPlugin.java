package io.lumine.mythic.lib.module;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class MMOPlugin extends JavaPlugin {
    private final NamespacedKey namespacedKey;

    public MMOPlugin() {
        this.namespacedKey = new NamespacedKey(this, "plugin");
    }

    /**
     * Does this plugin store data? This determines if MythicLib
     * must wait for this plugin to load their player data before
     * MythicLib marks the player session as ready.
     */
    // TODO really needed? all mmo plugins store player data
    public boolean hasData() {
        return true;
    }

    public boolean isProfilePlugin() {
        return false;
    }

    /**
     * @return If this plugin is a profile plugin
     */
    @Deprecated
    public boolean hasProfiles() {
        return isProfilePlugin();
    }

    @NotNull
    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }
}

