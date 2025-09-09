package io.lumine.mythic.lib.message.type;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.message.PlayerMessage;
import io.lumine.mythic.lib.message.ReadyMessage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleLineChatMessage extends PlayerMessage {
    private final String rawFormat;

    public SingleLineChatMessage(@NotNull String line) {
        this(new YamlConfiguration(), line);
    }

    public SingleLineChatMessage(@NotNull ConfigurationSection config, @NotNull String format) {
        super(config);

        this.rawFormat = format;
    }

    @Override
    public @NotNull ReadyMessage prepare(@Nullable ChatColor color, @Nullable Object... placeholders) {
        return new Ready(parsePlaceholders(rawFormat, color, placeholders));
    }

    @Override
    protected void onSend(@NotNull MMOPlayerData player, @Nullable ChatColor colorPrefix, @Nullable Object... placeholders) {
        var parsed = parsePlaceholders(rawFormat, colorPrefix, placeholders);
        this.sendPlayerMessage(player.getPlayer(), parsed);
    }

    class Ready extends ReadyMessage {
        final String format;

        Ready(String format) {
            this.format = format;
        }

        @Override
        public void send(@NotNull MMOPlayerData playerData) {
            this.send(playerData.getPlayer());
        }

        @Override
        public void send(@NotNull Player player) {
            SingleLineChatMessage.this.sendPlayerMessage(player, format);
        }

        @Override
        public @NotNull String getRawContent() {
            return format;
        }
    }
}
