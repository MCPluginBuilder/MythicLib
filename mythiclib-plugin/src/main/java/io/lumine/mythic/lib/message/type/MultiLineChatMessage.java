package io.lumine.mythic.lib.message.type;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.message.PlayerMessage;
import io.lumine.mythic.lib.message.ReadyMessage;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MultiLineChatMessage extends PlayerMessage {
    private final List<String> rawFormat;
    private final boolean jsonFormat;

    public MultiLineChatMessage(@NotNull List<String> rawFormat) {
        this(new YamlConfiguration(), rawFormat);
    }

    public MultiLineChatMessage(@NotNull ConfigurationSection config, @NotNull List<String> format) {
        super(config);

        this.rawFormat = format;
        this.jsonFormat = config.getBoolean("json", !this.rawFormat.isEmpty() && inferIsRawJson(this.rawFormat.get(0)));
    }

    @Override
    public @NotNull ReadyMessage prepare(@Nullable ChatColor color, @Nullable Object... placeholders) {
        var list = new ArrayList<String>(this.rawFormat.size());
        for (var line : rawFormat) list.add(this.parsePlaceholders(null, line, color, placeholders));
        return new Ready(list);
    }

    @Override
    protected void onSend(@NotNull MMOPlayerData player, @Nullable ChatColor colorPrefix, @Nullable Object... placeholders) {
        for (var line : rawFormat)
            this.sendPlayerMessage(player.getPlayer(), parsePlaceholders(player.getPlayer(), line, colorPrefix, placeholders), this.jsonFormat);
    }

    class Ready extends ReadyMessage {
        final List<String> format;

        Ready(List<String> format) {
            this.format = format;
        }

        @Override
        public void send(@NotNull MMOPlayerData player) {
            this.send(player.getPlayer());
        }

        @Override
        public void send(@NotNull Player player) {
            for (var line : format)
                MultiLineChatMessage.this.sendPlayerMessage(player, line, MultiLineChatMessage.this.jsonFormat);
        }

        @Override
        public @NotNull String getRawContent() {
            return Strings.join(this.format, '\n');
        }
    }
}
