package io.lumine.mythic.lib.message.type;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.message.PlayerMessage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
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
}
