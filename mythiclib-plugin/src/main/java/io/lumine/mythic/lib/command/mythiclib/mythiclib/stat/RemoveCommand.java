package io.lumine.mythic.lib.command.mythiclib.mythiclib.stat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RemoveCommand extends CommandTreeNode {
    private final Argument<@NotNull Player> argPlayer;
    private final Argument<@NotNull String> argStat;
    private final Argument<@NotNull String> argKey;

    public RemoveCommand(@NotNull CommandTreeNode parent) {
        super(parent, "remove");

        argPlayer = addArgument(Argument.PLAYER);
        argStat = addArgument(Argument.STAT);
        argKey = addArgument(Argument.MODIFIER_KEY.required());
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var target = explorer.parse(argPlayer);
        final var statName = explorer.parse(argStat);

        final var playerData = MMOPlayerData.get(target);
        final var key = explorer.parse(argKey);

        playerData.getStatMap().getInstance(statName).removeIf(key::equals);
        return CommandResult.SUCCESS;
    }
}