package io.lumine.mythic.lib.message;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.message.type.ActionBarMessage;
import io.lumine.mythic.lib.message.type.EmptyMessage;
import io.lumine.mythic.lib.message.type.MultiLineChatMessage;
import io.lumine.mythic.lib.message.type.SingleLineChatMessage;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.config.YamlUtils;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PlayerMessage {

    @Nullable
    private final SoundReader sound;

    @Nullable
    private final Script ranOnMessage;

    public PlayerMessage() {
        this.sound = null;
        this.ranOnMessage = null;
    }

    public PlayerMessage(@NotNull ConfigurationSection config) {
        ranOnMessage = config.contains("script") ? MythicLib.plugin.getSkills().loadScript(config.get("script")) : null;
        sound = SoundReader.fromConfig(config.get("sound"));
    }

    public void send(@NotNull MMOPlayerData player, @NotNull Object... placeholders) {
        send(player, null, placeholders);
    }

    public void send(@NotNull MMOPlayerData player, @Nullable ChatColor colorPrefix, @NotNull Object... placeholders) {
        if (!player.isOnline()) return; // Safeguard

        // Cast script if provided
        if (ranOnMessage != null) {
            ranOnMessage.cast(SkillMetadata.of(player));
        }

        // Play sound
        if (sound != null) sound.play(player.getPlayer());

        this.onSend(player, colorPrefix, placeholders);
    }

    protected abstract void onSend(@NotNull MMOPlayerData player, @Nullable ChatColor colorPrefix, @NotNull Object... placeholders);

    protected String parsePlaceholders(@Nullable Player player, @NotNull String input, @Nullable ChatColor colorPrefix, @NotNull Object... placeholders) {
        // TODO improve?

        // Apply color prefix
        if (colorPrefix != null) input = colorPrefix + input;

        // Apply PAPI placeholders
        if (player != null) input = MythicLib.plugin.getPlaceholderParser().parse(player, input);

        // Parse placeholders
        for (int j = 0; j < placeholders.length; j += 2)
            input = input.replace("{" + placeholders[j] + "}", String.valueOf(placeholders[j + 1]));

        return ChatColor.translateAlternateColorCodes('&', input);
    }

    protected void sendPlayerMessage(@NotNull Player player, @NotNull String message, boolean rawJson) {
        if (rawJson) MythicLib.plugin.getVersion().getWrapper().sendJson(player, message);
        else player.sendMessage(message);
    }

    @BackwardsCompatibility(version = "1.13 snapshots player message update")
    protected boolean inferIsRawJson(@NotNull String format) {
        return format.startsWith("{\"") || format.startsWith("[{\"");
    }

    @NotNull
    public static PlayerMessage fromConfig(@Nullable Object object) {

        // Lenient on null
        if (object == null) {
            return new EmptyMessage();
        }

        // String => single line message
        else if (object instanceof String) {
            var string = (String) object;

            // Empty string => empty message
            if (string.isEmpty()) return new EmptyMessage();

            // Starts with % => action bar
            if (string.startsWith("%")) return new ActionBarMessage(string.substring(1));

            // Fallback => single line chat message
            return new SingleLineChatMessage((String) object);
        }

        // List => list
        else if (object instanceof List) {
            return new MultiLineChatMessage((List<String>) object);
        }

        // Config => check for lines or single line
        else if (object instanceof ConfigurationSection) {
            var config = (ConfigurationSection) object;
            var format = YamlUtils.get(config, "message", "msg", "messages", "format", "fmt", "lines");

            // No message, but there can be sound, script...
            if (format == null) {
                return new EmptyMessage(config);
            }

            // Action bar !! single line only !!
            else if (config.getBoolean("action-bar")) {
                Validate.isInstanceOf(String.class, format, "Action bar messages only support single line strings");
                var string = (String) format;

                // Empty string => empty message
                if (string.isEmpty()) return new EmptyMessage(config);

                return new ActionBarMessage(config, (String) format);
            }

            // String => single line message
            // Starts with % => action bar message
            else if (format instanceof String) {
                var string = (String) format;

                // Empty string => empty message
                if (string.isEmpty()) return new EmptyMessage(config);

                // Starts with % => action bar
                if (string.startsWith("%")) return new ActionBarMessage(config, string.substring(1));

                // Fallback => single line chat message
                return new SingleLineChatMessage(config, (String) format);
            }

            // List => multi line chat message
            else if (format instanceof List) {
                return new MultiLineChatMessage(config, (List<String>) format);
            }

            // Invalid syntax
            else {
                throw new IllegalArgumentException("Invalid message syntax");
            }
        }

        // Invalid syntax
        else {
            throw new IllegalArgumentException("Invalid message syntax");
        }
    }
}
