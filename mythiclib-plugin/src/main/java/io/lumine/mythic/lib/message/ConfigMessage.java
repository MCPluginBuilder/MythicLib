package io.lumine.mythic.lib.message;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/*
 * TODO
 */
public class ConfigMessage {
    private final String format;
    private final boolean actionBar;

    @Nullable(value = "optional")
    private final SoundReader sound;

    @Nullable(value = "optional")
    private final Script ranOnMessage;

    public ConfigMessage(@NotNull Object object) {

        // Simple string
        if (object instanceof String) {
            String string = (String) object;
            format = string;
            ranOnMessage = null;
            actionBar = false;
            sound = null;
        }

        // From config
        else if (object instanceof ConfigurationSection) {
            ConfigurationSection config = (ConfigurationSection) object;
            format = Objects.requireNonNull(config.getString("format", config.getString("message")), "Message cannot be null");
            actionBar = config.getBoolean("action_bar", config.getBoolean("ab"));
            ranOnMessage = config.contains("script") ? MythicLib.plugin.getSkills().loadScript(config.get("script")) : null;
            sound = config.contains("sound") ? new SoundReader(config.get("sound")) : null;
        }

        // Error
        else throw new IllegalArgumentException("Expecting a string or config section");
    }

    @NotNull
    public String getFormat() {
        return format;
    }

    public boolean isActionBar() {
        return actionBar;
    }

    public void send(@NotNull Player player, @NotNull Object... toReplace) {
        send(player, null, toReplace);
    }

    public void send(@NotNull Player player, @Nullable ChatColor colorPrefix, @NotNull Object... toReplace) {

        // Format message
        String message = this.format;
        if (colorPrefix != null) message = colorPrefix + message;
        for (int j = 0; j < toReplace.length; j += 2)
            message = message.replace(String.valueOf(toReplace[j]), String.valueOf(toReplace[j + 1]));

        // Cast script if provided
        if (ranOnMessage != null) {
            Skill skill = new SimpleSkill(ranOnMessage);
            skill.cast(MMOPlayerData.get(player), TriggerType.PLUGIN);
        }

        // Play sound
        if (sound != null) sound.play(player);

        // Send to action bar
        if (actionBar) {
            // TODO
            if (MythicLib.plugin.getActionBarProvider() != null)
                MythicLib.plugin.getActionBarProvider().registerMessage();
            //if (Bukkit.getPluginManager().isPluginEnabled("MMOCore"))
            //    PlayerData.get(player).setLastActivity(PlayerActivity.ACTION_BAR_MESSAGE);
            VersionWrapper.get().sendActionBar(player, format);
            MythicLib.plugin.getVersion().getWrapper().sendActionBar(player, message);
        }

        // Send to chat
        else player.sendMessage(message);
    }
}
