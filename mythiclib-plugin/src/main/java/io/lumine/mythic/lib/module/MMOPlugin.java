package io.lumine.mythic.lib.module;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public boolean hasData() {
        return true;
    }

    /**
     * @return If this plugin is a profile plugin
     */
    public boolean isProfilePlugin() {
        return false;
    }

    @NotNull
    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    @NotNull
    public abstract SynchronizedDataManager<?, ?> getRawPlayerDataManager();

    public void debug(@NotNull String message) {
        UtilityMethods.debug(this, message);
    }

    public void debug(@Nullable String source, @NotNull String message) {
        UtilityMethods.debug(this, source, message);
    }

    //region Deprecated

    @Deprecated
    public boolean hasProfiles() {
        return isProfilePlugin();
    }

    //endregion
}

