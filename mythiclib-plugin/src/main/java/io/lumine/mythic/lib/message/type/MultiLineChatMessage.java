package io.lumine.mythic.lib.message.type;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.message.PlayerMessage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    protected void onSend(@NotNull MMOPlayerData player, @Nullable ChatColor colorPrefix, @Nullable Object... placeholders) {
        for (var line : rawFormat)
            this.sendPlayerMessage(player.getPlayer(), parsePlaceholders(player.getPlayer(), line, colorPrefix, placeholders), this.jsonFormat);
    }
}
