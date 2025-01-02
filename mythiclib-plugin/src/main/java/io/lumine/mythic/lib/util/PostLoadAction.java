package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class PostLoadAction {
    @Nullable
    private ConfigurationSection config;
    @Nullable
    private Consumer<ConfigurationSection> action;
    private final boolean persistent;

    public PostLoadAction(@NotNull Consumer<ConfigurationSection> action) {
        this(false, action);
    }

    /**
     * Objects which must load some data afterwards, like quests which must load
     * their parent quests after all quests were initialized or player classes
     * which must load their subclasses.
     * <p>
     * The general use case is when some plugin as an object registry where the
     * configuration of the objects being registered rely on the SAME registry.
     *
     * @param persistent Can the object be postloaded multiple times?
     * @param action     Action to be performed after loading the reference only
     */
    public PostLoadAction(boolean persistent, @NotNull Consumer<ConfigurationSection> action) {
        this.persistent = persistent;
        this.action = Objects.requireNonNull(action, "Action cannot be null");
    }

    public void cacheConfig(@Nullable ConfigurationSection config) {
        if (action == null) throw new PostLoadException("Object already post loaded");

        Validate.isTrue(this.config == null, "Config already cached");
        if (config != null) this.config = config;
    }

    @Nullable
    public ConfigurationSection getCachedConfig() {
        if (action == null) throw new PostLoadException("Object already post loaded");

        return config;
    }

    public void performAction() {
        if (action == null) throw new PostLoadException("Object already post loaded");

        // Persistence update
        final Consumer<ConfigurationSection> action = this.action;
        if (!persistent) this.action = null;

        // Post-load if necessary
        if (config != null) {
            final ConfigurationSection config = this.config;
            this.config = null;
            action.accept(config); // Last for exception handling
        }
    }
}
