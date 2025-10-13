package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.command.argument.Arguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HealthScaleCommand extends CommandTreeNode {
    public HealthScaleCommand(CommandTreeNode parent) {
        super(parent, "health-scale");

        addChild(new SetCommandNode(this));
        addChild(new ResetCommandNode(this));
    }

    static class SetCommandNode extends CommandTreeNode {
        private final Argument<@NotNull Double> argAmount;
        private final Argument<@NotNull Player> argPlayer;

        public SetCommandNode(CommandTreeNode parent) {
            super(parent, "set");

            argAmount = addArgument(Argument.AMOUNT_DOUBLE);
            argPlayer = addArgument(Argument.PLAYER_OR_SENDER);
        }

        @Override
        public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
            final double scale = explorer.parse(argAmount);
            Arguments.isTrue(scale > 0, "Scale must be positive");
            final var target = explorer.parse(argPlayer);

            // enable health scale
            target.setHealthScaled(true);
            target.setHealthScale(scale);
            return explorer.success("Health scale of &6" + scale + "&e HP enabled for player &6" + target.getName());
        }
    }

    static class ResetCommandNode extends CommandTreeNode {
        private final Argument<@NotNull Player> argPlayer;

        public ResetCommandNode(CommandTreeNode parent) {
            super(parent, "reset");

            argPlayer = addArgument(Argument.PLAYER_OR_SENDER);
        }

        @Override
        public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
            final var target = explorer.parse(argPlayer);

            // Disable health scale
            target.setHealthScaled(false);
            return explorer.success("Health scale disabled for player &6" + target.getName());
        }
    }
}
