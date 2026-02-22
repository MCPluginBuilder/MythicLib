package io.lumine.mythic.lib.command.mythiclib.mythiclib.cooldown;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckCommand extends CommandTreeNode {
    private final Argument<@NotNull String> argKey;
    private final Argument<@NotNull Player> argPlayer;

    public CheckCommand(@NotNull CommandTreeNode parent) {
        super(parent, "check");

        argKey = addArgument(Argument.COOLDOWN_CURRENT);
        argPlayer = addArgument(Argument.PLAYER_OR_SENDER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var key = explorer.parse(argKey);
        final var playerData = MMOPlayerData.get(explorer.parse(argPlayer));

        final var info = playerData.getCooldownMap().getInfo(key);
        if (info == null || info.hasEnded())
            return explorer.success("Player &6" + playerData.getPlayerName() + "&e has no active cooldown with key &6" + key);

        return explorer.success("Player &6" + playerData.getPlayerName() + "&e has remaining cooldown &6" + (info.getRemaining() / 1000d) + " sec&e for key &6" + key);
    }
}