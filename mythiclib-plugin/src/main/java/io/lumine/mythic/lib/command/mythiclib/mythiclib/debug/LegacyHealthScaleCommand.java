package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class LegacyHealthScaleCommand extends CommandTreeNode {
    public LegacyHealthScaleCommand(CommandTreeNode parent) {
        super(parent, "healthscale");

        addArgument(Argument.AMOUNT_INT);
        addArgument(Argument.PLAYER_OR_SENDER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {

        if (args.length < 3)
            return CommandResult.THROW_USAGE;

        if (!(sender instanceof Player) && args.length < 4) {
            MythicLib.plugin.getLogger().info("Please specify a player.");
            return CommandResult.FAILURE;
        }

        double scale;
        try {
            scale = Double.parseDouble(args[2]);
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().info("Could not read scale amount");
            return CommandResult.FAILURE;
        }

        final Player target = args.length > 3 ? Bukkit.getPlayer(args[3]) : (Player) sender;
        if (target == null) {
            MythicLib.plugin.getLogger().info("Could not find player " + args[3] + ".");
            return CommandResult.FAILURE;
        }

        target.setHealthScaled(true);
        target.setHealthScale(scale);
        MythicLib.plugin.getLogger().info(args[0] + " Health has been scaled!");
        return CommandResult.SUCCESS;
    }
}
