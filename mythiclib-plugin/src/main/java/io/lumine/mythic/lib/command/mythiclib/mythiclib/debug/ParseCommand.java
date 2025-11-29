package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.util.formula.NumericalExpression;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ParseCommand extends CommandTreeNode {
    public ParseCommand(CommandTreeNode parent) {
        super(parent, "parse");
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final String expression = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        // TODO use stat formula instead
        sender.sendMessage(String.valueOf(NumericalExpression.eval(expression)));
        return CommandResult.SUCCESS;
    }
}
