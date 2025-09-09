package io.lumine.mythic.lib.message.type;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.message.PlayerMessage;
import io.lumine.mythic.lib.message.ReadyMessage;
import io.lumine.mythic.lib.message.actionbar.ActionBarHandler;
import io.lumine.mythic.lib.message.actionbar.ActionBarPriority;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionBarMessage extends PlayerMessage {
    private final String rawFormat;
    private final int actionBarPriority;
    private final long duration;

    public ActionBarMessage(@NotNull String format) {
        this(new YamlConfiguration(), format);
    }

    public ActionBarMessage(@NotNull ConfigurationSection config, @NotNull String format) {
        super(config);

        this.rawFormat = format;
        this.actionBarPriority = config.getInt("priority", ActionBarPriority.NORMAL);
        this.duration = config.getLong("duration", ActionBarHandler.DEFAULT_TIME_OUT);
    }

    @Override
    public @NotNull ReadyMessage prepare(@Nullable ChatColor color, @Nullable Object... placeholders) {
        return new Ready(parsePlaceholders(rawFormat, color, placeholders));
    }

    @Override
    protected void onSend(@NotNull MMOPlayerData player, @Nullable ChatColor colorPrefix, @Nullable Object... placeholders) {
        var parsed = parsePlaceholders(rawFormat, colorPrefix, placeholders);
        player.getActionBar().show(actionBarPriority, duration, parsed);
    }

    class Ready extends ReadyMessage {
        final String format;

        Ready(String format) {
            this.format = format;
        }

        @Override
        public void send(@NotNull MMOPlayerData playerData) {
            playerData.getActionBar().show(actionBarPriority, duration, format);
        }

        @Override
        public void send(@NotNull Player player) {
            this.send(MMOPlayerData.get(player));
        }

        @Override
        public @NotNull String getRawContent() {
            return this.format;
        }
    }
}
