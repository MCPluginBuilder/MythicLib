package io.lumine.mythic.lib.api.util;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @deprecated See {@link io.lumine.mythic.lib.util.PreloadedObject}
 */
@Deprecated
public abstract class PostLoadObject {

    @Nullable
    private ConfigurationSection config;

    /**
     * Objects which must load some data afterwards, like quests which must load
     * their parent quests after all quests were initialized or player classes
     * which must load their subclasses.
     * <p>
     * The general use case is when some plugin as an object registry where the
     * configuration of the objects being registered rely on the SAME registry.
     * <p>
     * The {@link #postLoad()} method is meant to be used only once. It calls
     * the {@link #whenPostLoaded(ConfigurationSection)} method, passing in as
     * argument the cached config; and then sends it to garbage collection.
     *
     * @param config Config section being cached in
     *               memory until {@link #postLoad()} is called
     */
    @Deprecated
    public PostLoadObject(@Nullable ConfigurationSection config) {
        this.config = config;
    }

    @NotNull
    @Deprecated
    public ConfigurationSection getPostLoadConfig() {
        return Objects.requireNonNull(config, "Object already post loaded");
    }

    @Deprecated
    public void postLoad() {
        Validate.notNull(config, "Object already post loaded");
        whenPostLoaded(config);
        config = null;
    }

    @Deprecated
    protected abstract void whenPostLoaded(@NotNull ConfigurationSection config);
}
