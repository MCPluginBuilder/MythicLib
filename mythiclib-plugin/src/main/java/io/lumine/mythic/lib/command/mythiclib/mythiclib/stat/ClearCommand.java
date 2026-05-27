package io.lumine.mythic.lib.command.mythiclib.mythiclib.stat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClearCommand extends CommandTreeNode {
    private final Argument<@NotNull Player> argPlayer;
    private final Argument<@NotNull String> argKey;

    public ClearCommand(@NotNull CommandTreeNode parent) {
        super(parent, "clear");

        argPlayer = addArgument(Argument.PLAYER);
        argKey = addArgument(Argument.modifierKeyOf(argPlayer));
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var target = explorer.parse(argPlayer);

        final var playerData = MMOPlayerData.get(target);
        final var key = explorer.parse(argKey);

        for (var instance : playerData.getStatMap().getInstances())
            instance.removeIf(key::equals);
        return CommandResult.SUCCESS;
    }
}
