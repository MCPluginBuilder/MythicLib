package io.lumine.mythic.lib.message.type;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.message.PlayerMessage;
import io.lumine.mythic.lib.message.ReadyMessage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyMessage extends PlayerMessage {
    public EmptyMessage() {
        super();
    }

    public EmptyMessage(@NotNull ConfigurationSection config) {
        super(config);
    }

    @Override
    protected void onSend(@NotNull MMOPlayerData player, @Nullable ChatColor colorPrefix, @Nullable Object... placeholders) {
        // Nothing
    }

    @Override
    public @NotNull ReadyMessage prepare(@Nullable ChatColor color, @Nullable Object... placeholders) {
        return new Ready();
    }

    static class Ready extends ReadyMessage {

        @Override
        public void send(@NotNull MMOPlayerData playerData) {
            // Do nothing
        }

        @Override
        public void send(@NotNull Player player) {
            // Do nothing
        }

        @Override
        public @NotNull String getRawContent() {
            return "";
        }
    }
}
