package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InfoCommand extends CommandTreeNode {
    private final Argument<Player> argPlayer;

    public InfoCommand(CommandTreeNode parent) {
        super(parent, "info");

        argPlayer = addArgument(Argument.PLAYER_OR_SENDER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        var player = MMOPlayerData.get(explorer.parse(argPlayer));

        var classes = MythicLib.plugin.getClassModule();
        var levels = MythicLib.plugin.getLevelModule();
        var resources = MythicLib.plugin.getManaModule();

        sender.sendMessage("| RPG Player Info of " + ChatColor.YELLOW + player.getPlayerName());
        sender.sendMessage("|");
        sender.sendMessage("| Class Provider: " + ChatColor.YELLOW + classes.getClass().getSimpleName());
        sender.sendMessage("| Level Provider: " + ChatColor.YELLOW + levels.getClass().getSimpleName());
        sender.sendMessage("| Mana Provider: " + ChatColor.YELLOW + resources.getClass().getSimpleName());
        sender.sendMessage("|");
        sender.sendMessage("| Player Class: " + ChatColor.YELLOW + classes.getClass(player));
        sender.sendMessage("| Player Level: " + ChatColor.YELLOW + levels.getLevel(player));
        sender.sendMessage("| Current Mana: " + ChatColor.YELLOW + resources.getMana(player));
        sender.sendMessage("| Current Stamina: " + ChatColor.YELLOW + resources.getStamina(player));

        return CommandResult.SUCCESS;
    }
}
