package io.lumine.mythic.lib.comp.placeholder.api;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class PluginPlaceholderExpansion<T> extends PlaceholderExpansion {
    private final JavaPlugin owner;
    private final String identifier;

    private final Map<String, PlaceholderEntry<T>> BY_ID = new HashMap<>();

    public PluginPlaceholderExpansion(JavaPlugin owner) {
        this.owner = owner;
        this.identifier = owner.getName().toLowerCase();

        // Bake placeholder map
        for (var placeholder : getPlaceholderRegistry()) BY_ID.put(placeholder.getPrefix(), placeholder);
    }

    @NotNull
    protected abstract Iterable<PlaceholderEntry<T>> getPlaceholderRegistry();

    @NotNull
    protected abstract T getPlayerData(OfflinePlayer player);

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Indyuce";
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getVersion() {
        return owner.getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String raw) {
        PlaceholderEntry<T> found = null;
        int endIndex = -1;

        // Find the longest matching placeholder
        for (int i = 0; i < raw.length(); i++)
            if (raw.charAt(i) == '_' || i == raw.length() - 1) {
                final var prefix = raw.substring(0, i + 1);
                final var mapping = BY_ID.get(prefix);
                if (mapping != null) {
                    found = mapping;
                    endIndex = i + 1;
                }
            }

        // No placeholder matches
        if (found == null) return PLACEHOLDER_NOT_FOUND;

        // Retrieve player data only if necessary
        T playerData;
        try {
            playerData = getPlayerData(player);
        } catch (Exception exception) {
            if (found.requiresPlayer()) return NO_PLAYER_PLACEHOLDER;
            playerData = null;
        }

        try {
            // Try to parse placeholder
            final var meta = new PlaceholderMetadata<>(playerData, raw, endIndex);
            return found.parse(meta);
        } catch (Exception throwable) {
            // Fallback value
            return found.getFallback();
        }
    }

    protected static final String NO_PLAYER_PLACEHOLDER = "OfflinePlayer";
    protected static final String PLACEHOLDER_NOT_FOUND = "NoMatch";
}
