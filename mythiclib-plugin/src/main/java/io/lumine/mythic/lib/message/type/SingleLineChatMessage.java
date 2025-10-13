package io.lumine.mythic.lib.message.type;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.message.PlayerMessage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleLineChatMessage extends PlayerMessage {
    private final String rawFormat;
    private final boolean jsonFormat;

    public SingleLineChatMessage(@NotNull String line) {
        this(new YamlConfiguration(), line);
    }

    public SingleLineChatMessage(@NotNull ConfigurationSection config, @NotNull String format) {
        super(config);

        this.rawFormat = format;
        this.jsonFormat = config.getBoolean("json", this.inferIsRawJson(format));
    }

    @Override
    protected void onSend(@NotNull MMOPlayerData player, @Nullable ChatColor colorPrefix, @Nullable Object... placeholders) {
        var parsed = parsePlaceholders(player.getPlayer(), rawFormat, colorPrefix, placeholders);
        this.sendPlayerMessage(player.getPlayer(), parsed, this.jsonFormat);
    }
}
