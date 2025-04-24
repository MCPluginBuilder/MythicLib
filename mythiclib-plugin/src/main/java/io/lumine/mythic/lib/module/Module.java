package io.lumine.mythic.lib.module;

import io.lumine.mythic.lib.util.annotation.NotUsed;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A module is defined as a standalone feature in the plugin. It can
 * depend on other modules to operate, and can be disabled/enabled
 * at runtime based on configuration files.
 */
@NotUsed
public abstract class Module {
    protected final MMOPluginImpl plugin;
    protected final NamespacedKey key;
    protected final List<NamespacedKey> dependencies = new ArrayList<>();
    //protected final List<Module> resolvedDependencies = new ArrayList<>();

    // Runtime flags
    // TODO set loaded to false.
    protected boolean loaded = true, enabled, started;

    /**
     * Module was completely disabled either during loading or enabling phase
     */
    protected boolean disabled;

    protected Module(MMOPluginImpl plugin) {
        this.plugin = plugin;

        ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);
        Validate.notNull(info, "Could not find annotation data ModuleInfo");
        this.key = NamespacedKey.fromString(info.key(), plugin);
    }

    protected Module(@NotNull MMOPluginImpl plugin, @NotNull String key, boolean load) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, key);
    }

    /*
    public void resolveDependencies() {
        Validate.isTrue(MMOPluginRegistry.getInstance().isRegistrationAllowed(), "Dependency validation is not allowed");

        dependencies.removeIf(dep -> {
            final String pluginId = dep.split("\\:")[0];
            return !activePlugins.contains(pluginId);
        });
    }*/

    @Deprecated
    public boolean isEnabled() {
        return enabled;
    }

    @Deprecated
    public boolean shouldLoad() {
        // Default impl
        return true;
    }

    /**
     * Loading a manager refers to instantiating all the necessary
     * references potentially needed everywhere else. This is usually
     * done when plugins are loading, not enabling
     */
    @Deprecated
    public void load() {
        Validate.isTrue(!loaded, "Module is already loaded");

        this.loaded = true;
        onLoad();
    }

    @Deprecated
    public boolean shouldEnable() {
        // Default impl
        return true;
    }

    /**
     * Called when the server starts or when the plugins are getting enabled.
     */
    public void enable() {
        Validate.isTrue(loaded, "Module is not loaded yet");
        Validate.isTrue(!enabled, "Module is already enabled");

        // Startup script
        if (!started) {
            onStartup();
            started = true;
        }

        // Register listener if necessary
        if (this instanceof Listener) Bukkit.getPluginManager().registerEvents((Listener) this, plugin);

        // Flags
        this.enabled = true;

        // Callback
        onEnable();
    }

    /**
     * Called before reloading
     */
    public void reset() {
        Validate.isTrue(loaded || enabled, "Module is already reset");

        // Unregister listeners if necessary
        if (this instanceof Listener) HandlerList.unregisterAll((Listener) this);

        // Flags
        this.loaded = false;
        this.enabled = false;

        // Callback
        onReset();
    }

    @Deprecated
    public void onLoad() {
        // Default impl
    }

    public void onEnable() {
        // Default impl
    }

    /**
     * Gets called at most once
     */
    public void onStartup() {
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
    public NamespacedKey getKey() {
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
