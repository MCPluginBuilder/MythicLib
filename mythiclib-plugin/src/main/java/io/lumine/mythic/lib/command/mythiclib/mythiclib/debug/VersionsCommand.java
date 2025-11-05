package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class VersionsCommand extends CommandTreeNode {
    public VersionsCommand(CommandTreeNode parent) {
        super(parent, "versions");
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {

        MythicLib.plugin.getLogger().log(Level.INFO, "Plugin versions:");
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            MythicLib.plugin.getLogger().log(Level.INFO, "> " + plugin.getName() + " " + plugin.getDescription().getVersion()
                    + " by " + String.join(",", plugin.getDescription().getAuthors())
                    + (Bukkit.getPluginManager().isPluginEnabled(plugin) ? "" : " (Disabled)"));

        return explorer.success("Plugin versions pasted to server console");
    }
}
