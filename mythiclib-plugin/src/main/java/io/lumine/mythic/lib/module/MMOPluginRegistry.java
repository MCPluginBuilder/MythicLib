package io.lumine.mythic.lib.module;

import io.lumine.mythic.lib.util.annotation.NotUsed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NotUsed
public class MMOPluginRegistry {
    private final Map<String, MMOPluginImpl> plugins = new HashMap<>();

    /**
     * Sorted list including all managers from all registered MMO plugins
     */
    private final List<Module> sorted = new ArrayList<>();

    private final Map<String, Module> moduleRegistry = new HashMap<>();

    private boolean registration = true;

    private static MMOPluginRegistry instance;

    private MMOPluginRegistry() {
        // Singleton pattern
    }

    @NotNull
    public static MMOPluginRegistry getInstance() {
        if (instance == null) instance = new MMOPluginRegistry();
        return instance;
    }

    @Nullable
    public MMOPluginImpl getPlugin(@NotNull String key) {
        return plugins.get(key);
    }

    public void registerPlugin(@NotNull MMOPluginImpl plugin) {
        this.plugins.put(plugin.getName(), plugin);
    }

    public boolean isRegistrationAllowed() {
        return registration;
    }

    /*
    public void prepareManagers() {
        Validate.isTrue(registration, "Managers were already prepared");

        // Validate all managers
        final Set<String> activePluginIds = plugins.stream().map(plugin -> lowerCase(plugin.getName())).collect(Collectors.toSet());
        plugins.forEach(plugin -> plugin.getManagers().forEach(manager -> manager.validateDependencies(activePluginIds)));

        // Topological sorting
        sortManagers();

        // Prepare all managers


        registration = false;
    }
    */

    private void sortManagers() {
        // TODO topological sorting of all managers based on dependencies
    }

    @NotNull
    private static String lowerCase(@NotNull String str) {
        return str.toLowerCase().replace(" ", "_").replace("-", "_").replaceAll("[^a-z_]", "");
    }
}
