package io.lumine.mythic.lib.util.config;

import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class ConfigVersioner {

    private static Runnable NOP = () -> {
    };

    @BackwardsCompatibility(version = "unspecified")
    public static List<Runnable> nops(int nops, Runnable... entries) {
        final var result = new ArrayList<Runnable>();
        for (int i = 0; i < nops; i++) result.add(NOP);
        Collections.addAll(result, entries);
        return result;
    }

    public static void applyConfigVersioner(@NotNull Plugin plugin, @NotNull List<Runnable> entries) {

        // Retrieve current config version
        final var currentConfigVersion = plugin.getConfig().getInt("config-version", 0);
        if (currentConfigVersion >= entries.size()) return; // Makes no sense, fail silently.

        // Apply all version updates that haven't been applied yet
        for (int i = currentConfigVersion; i < entries.size(); i++)
            try {
                plugin.getLogger().log(Level.INFO, "Applying config update n" + (i + 1) + "...");
                entries.get(i).run();
            } catch (Exception throwable) {
                plugin.getLogger().log(Level.WARNING, "Failed to apply config update n" + (i + 1) + ":");
                throwable.printStackTrace();
            }

        plugin.reloadConfig(); // Very important, in case it was edited in the meantime.
        plugin.getConfig().set("config-version", entries.size()); // Increment config version
        plugin.saveConfig(); // Save config
    }
}
