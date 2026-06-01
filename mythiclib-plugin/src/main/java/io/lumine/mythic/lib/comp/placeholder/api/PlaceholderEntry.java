package io.lumine.mythic.lib.comp.placeholder.api;

import org.jetbrains.annotations.NotNull;

public interface PlaceholderEntry<T> {

    public String getPrefix();

    public String getFallback();

    // default for backwards compatibility
    default boolean requiresPlayer() {
        return true;
    }

    @NotNull
    public String parse(@NotNull PlaceholderMetadata<T> metadata);
}
