package io.lumine.mythic.lib.module;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A module is defined as a standalone feature in the plugin. It can
 * depend on other modules to operate, and can be disabled/enabled
 * at runtime based on configuration files.
 */
public abstract class Module {
    protected final MMOPlugin plugin;
    private final NamespacedKey key;

    // Runtime flags
    // enabled = if the module is running
    // startup = toggled on once while calling #onStartup()
    private boolean enabled, startup;

    private final List<Listener> moduleListeners = new ArrayList<>();
    private final List<ListenerToggle> moduleListenerToggles = new ArrayList<>();

    protected Module(@NotNull MMOPlugin plugin) {
        this.plugin = plugin;

        ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);
        Validate.notNull(info, "Could not find annotation data ModuleInfo");
        this.key = NamespacedKey.fromString(info.key(), plugin);

        Validate.isTrue(!(this instanceof Listener), "Module cannot be a Listener, use inner class");
    }

    private void resolveModuleListeners() {
        for (var field : getClass().getDeclaredFields())
            try {
                final var annot = field.getAnnotation(ModuleListener.class);
                if (annot == null) continue;

                field.setAccessible(true);
                if (Listener.class.isAssignableFrom(field.getType()))
                    this.moduleListeners.add((Listener) Objects.requireNonNull(field.get(this)));
                else if (ListenerToggle.class.isAssignableFrom(field.getType())) {
                    this.moduleListenerToggles.add((ListenerToggle) Objects.requireNonNull(field.get(this)));
                } else
                    throw new IllegalStateException("Unsupported module listener field type " + field.getType().getName());
                field.setAccessible(false);
            } catch (Exception exception) {
                throw new RuntimeException("Could not resolve module listeners", exception);
            }
    }

    //region FSM

    public boolean isEnabled() {
        return enabled;
    }

    protected boolean shouldEnable() {
        // Override, check the config if it should enable
        return true;
    }

    /**
     * Called when the plugin starts or when plugins reload. This
     * checks the enable toggle in the config, if it does not match
     * the module state, then the module is either enabled or disabled.
     * <p>
     * If the module needs to be enabled, its configuration is reloaded.
     */
    public void reload() {

        // Startup, run at most once
        if (!startup) startup();

        // Module should be disabled
        if (shouldEnable()) {

            // Enable module
            if (enabled) onReset();
            else enable();

            // Reload config
            onReload();
        }

        // Module should be disabled
        else {
            if (enabled) {
                onReset();
                disable();
            }
        }
    }

    private void startup() {
        Validate.isTrue(!startup, "Module has already started up");
        startup = true;

        resolveModuleListeners();
        onStartup();
    }

    private void enable() {
        Validate.isTrue(!enabled, "Module is already enabled");
        enabled = true;

        moduleListeners.forEach(listener -> plugin.getServer().getPluginManager().registerEvents(listener, plugin));
        onEnable();
    }

    private void disable() {
        Validate.isTrue(enabled, "Module is already disabled");
        enabled = false;

        moduleListeners.forEach(HandlerList::unregisterAll);
        moduleListenerToggles.forEach(ListenerToggle::disable);
        onDisable();
    }

    //endregion

    //region Methods

    /**
     * Gets called at most once the first time the module is enabled.
     * This can be used to generate default config files or run
     * static initialization code or computations.
     */
    protected void onStartup() {
        // Default impl
    }

    /**
     * Called before the module is re-enabled. This method should typically
     * empty maps or structures that were filled during #onEnable().
     * <p>
     * At the end of this method, the module should be ready to be either
     * enabled or disabled.
     */
    protected void onReset() {
        // Default impl
    }

    protected void onEnable() {
        // Default impl
    }

    protected void onDisable() {
        // Default impl
    }

    /**
     * Called last everytime, after the module has been either
     * reset or enabled.
     */
    protected void onReload() {
        // Default impl
    }

    //endregion>

    /*
    //region Dependencies

    protected final List<NamespacedKey> dependencies = new ArrayList<>();
    protected final List<Module> resolvedDependencies = new ArrayList<>();

    public void resolveDependencies() {
        Validate.isTrue(MMOPluginRegistry.getInstance().isRegistrationAllowed(), "Dependency validation is not allowed");

        dependencies.removeIf(dep -> {
            final String pluginId = dep.split("\\:")[0];
            return !activePlugins.contains(pluginId);
        });
    }

    public void addDependencies(NamespacedKey... dependencies) {
        Collections.addAll(this.dependencies, dependencies);
    }

    @NotNull
    public List<NamespacedKey> getDependencies() {
        return dependencies;
    }

    //endregion
    */

    @NotNull
    public NamespacedKey getModuleKey() {
        return key;
    }

    @NotNull
    public MMOPlugin getPlugin() {
        return plugin;
    }
}
