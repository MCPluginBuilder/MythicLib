package io.lumine.mythic.lib.command.mythiclib.mythiclib.cooldown;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ApplyCommand extends CommandTreeNode {
    private final Argument<@NotNull String> argKey;
    private final Argument<@NotNull Player> argPlayer;
    private final Argument<@NotNull Long> argDuration;

    public ApplyCommand(@NotNull CommandTreeNode parent) {
        super(parent, "apply");

        argKey = addArgument(Argument.COOLDOWN_CURRENT);
        argPlayer = addArgument(Argument.PLAYER);
        argDuration = addArgument(Argument.DURATION_TICKS);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var key = explorer.parse(argKey);
        final var playerData = MMOPlayerData.get(explorer.parse(argPlayer));
        final var duration = explorer.parse(argDuration);

        playerData.getCooldownMap().applyCooldown(key, duration / 20d);
        return explorer.success("Player &6" + playerData.getPlayerName() + "&e now has cooldown &6" + duration + " ticks&e with key &6" + key);
    }
}