package io.lumine.mythic.lib.command.mythiclib.mythiclib.stat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckCommand extends CommandTreeNode {
    private final Argument<@NotNull Player> argPlayer;
    private final Argument<@NotNull String> argStat;

    public CheckCommand(@NotNull CommandTreeNode parent) {
        super(parent, "check");

        argPlayer = addArgument(Argument.PLAYER);
        argStat = addArgument(Argument.STAT);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var target = explorer.parse(argPlayer);

        final var playerData = MMOPlayerData.get(target);
        final var stat = explorer.parse(argStat);

        return explorer.success("Player &6" + target.getName() + "&e has stat &6" + stat + "&e value &6" + playerData.getStatMap().getInstance(stat).getFinal());
    }
}