package io.lumine.mythic.lib.util;

import org.bukkit.plugin.java.JavaPlugin;

public class MMOPlugin extends JavaPlugin {

    /**
     * Does this plugin store data? This determines if MythicLib
     * must wait for this plugin to load their player data before
     * MythicLib marks the player session as alive and ready.
     */
    public boolean hasData() {
        return true;
    }

    /**
     * @return If this plugin is a profile plugin
     */
    public boolean hasProfiles() {
        return false;
    }
}
