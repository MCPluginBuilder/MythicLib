package io.lumine.mythic.lib.command.mythiclib.mythiclib;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends CommandTreeNode {
    public ReloadCommand(CommandTreeNode parent) {
        super(parent, "reload");
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        MythicLib.plugin.reload();
        return explorer.success("MythicLib reloaded successfully");
    }
}
