package io.lumine.mythic.lib.command.mythiclib.mythiclib.cooldown;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResetCommand extends CommandTreeNode {
    private final Argument<@NotNull String> argKey;
    private final Argument<@NotNull Player> argPlayer;

    public ResetCommand(@NotNull CommandTreeNode parent) {
        super(parent, "reset");

        argKey = addArgument(Argument.COOLDOWN_CURRENT);
        argPlayer = addArgument(Argument.PLAYER_OR_SENDER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var key = explorer.parse(argKey);
        final var playerData = MMOPlayerData.get(explorer.parse(argPlayer));

        final var cdInfo = playerData.getCooldownMap().getInfo(key);
        final var cleared = cdInfo != null && !cdInfo.hasEnded();
        playerData.getCooldownMap().resetCooldown(key);
        return explorer.success(cleared
                ? "Reset cooldown &6" + key + "&e for player &6" + playerData.getPlayerName()
                : "Player &6" + playerData.getPlayerName() + "&e had no cooldown with key &6" + key);
    }
}