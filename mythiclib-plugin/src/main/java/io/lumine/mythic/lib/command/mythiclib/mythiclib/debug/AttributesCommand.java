package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.gui.builtin.AttributeExplorer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AttributesCommand extends CommandTreeNode {
    private final Argument<@NotNull Player> argPlayer;

    public AttributesCommand(CommandTreeNode parent) {
        super(parent, "attributes");

        argPlayer = addArgument(Argument.PLAYER_OR_SENDER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            return explorer.fail("This command is only for players.");
        }

        final var target = explorer.parse(argPlayer);
        new AttributeExplorer((Player) sender, target).open();
        return CommandResult.SUCCESS;
    }
}
