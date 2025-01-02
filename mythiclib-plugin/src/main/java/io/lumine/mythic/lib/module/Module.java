package io.lumine.mythic.lib.module;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A module is defined as a standalone feature in the plugin. It can
 * depend on other modules to operate, and can be disabled/enabled
 * at runtime based on configuration files.
 */
public abstract class Module {
    protected final MMOPluginImpl plugin;
    protected final NamespacedKey key;
    protected final List<NamespacedKey> dependencies = new ArrayList<>();
    protected final boolean load;

    protected final List<Module> resolvedDependencies = new ArrayList<>();

    // Runtime flags
    protected boolean loaded, enabled;

    protected Module(MMOPluginImpl plugin) {
        this.plugin = plugin;

        ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);
        Validate.notNull(info, "Could not find annotation data ModuleInfo");
        this.key = NamespacedKey.fromString(info.key(), plugin);
        this.load = info.load();
    }

    protected Module(@NotNull MMOPluginImpl plugin, @NotNull String key, boolean load) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, key);
        this.load = true;
    }

    /*
    public void resolveDependencies() {
        Validate.isTrue(MMOPluginRegistry.getInstance().isRegistrationAllowed(), "Dependency validation is not allowed");

        dependencies.removeIf(dep -> {
            final String pluginId = dep.split("\\:")[0];
            return !activePlugins.contains(pluginId);
        });
    }*/

    public boolean isLoaded() {
        return !load || loaded;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Loading a manager refers to instantiating all the necessary
     * references potentially needed everywhere else. This is usually
     * done when plugins are loading, not enabling
     */
    public void load() {
        Validate.isTrue(!loaded, "Module is already loaded");

        this.loaded = true;
        onLoad();
    }

    /**
     * Called when the server starts or when the plugins are loaded.
     */
    public void enable() {
        Validate.isTrue(!load || loaded, "Module is not loaded yet");
        Validate.isTrue(!enabled, "Module is already enabled");

        this.enabled = true;
        onEnable();
    }

    /**
     * Called before reloading
     */
    public void reset() {
        Validate.isTrue(loaded || enabled, "Module is already reset");

        this.loaded = false;
        this.enabled = false;
        onReset();
    }

    public void onLoad() {
        // Default impl
    }

    public void onEnable() {
        // Default impl
    }

    public void onReset() {
        // Default impl
    }

    public void addDependencies(NamespacedKey... dependencies) {
        Collections.addAll(this.dependencies, dependencies);
    }

    @NotNull
    public List<NamespacedKey> getDependencies() {
        return dependencies;
    }

    @NotNull
    public NamespacedKey getNamespacedKey() {
        return key;
    }

    @NotNull
    public MMOPluginImpl getPlugin() {
        return plugin;
    }

    /**
     * Reloads this specific module. It could point to outdated references
     * to other managers though, so it is recommended to fully reload all the
     * MMO plugins when doing big changes. This can be done by using /ml reload.
     */
    public void reload() {
        reset();
        load();
        enable();
    }
}
