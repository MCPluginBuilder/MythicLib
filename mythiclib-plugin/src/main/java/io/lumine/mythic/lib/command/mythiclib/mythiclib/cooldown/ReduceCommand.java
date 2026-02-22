package io.lumine.mythic.lib.command.mythiclib.mythiclib.cooldown;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class ReduceCommand extends CommandTreeNode {
    public ReduceCommand(@NotNull CommandTreeNode parent) {
        super(parent, "reduce");

        addChild(new Flat(this));
        addChild(new Percent(this, "initial", CooldownInfo::reduceInitialCooldown));
        addChild(new Percent(this, "remaining", CooldownInfo::reduceRemainingCooldown));
    }

    abstract static class AbstractReduce extends CommandTreeNode {
        protected final Argument<@NotNull String> argKey;
        protected final Argument<@NotNull Player> argPlayer;

        public AbstractReduce(@NotNull CommandTreeNode parent, String name) {
            super(parent, name);

            argKey = addArgument(Argument.COOLDOWN_CURRENT);
            argPlayer = addArgument(Argument.PLAYER);
        }
    }

    static class Flat extends AbstractReduce {
        private final Argument<@NotNull Long> argDuration;

        public Flat(@NotNull CommandTreeNode parent) {
            super(parent, "flat");

            argDuration = addArgument(Argument.DURATION_TICKS);
        }

        @Override
        public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
            final var key = explorer.parse(argKey);
            final var playerData = MMOPlayerData.get(explorer.parse(argPlayer));

            final var info = playerData.getCooldownMap().getInfo(key);
            if (info == null || info.hasEnded())
                return explorer.success("Player &6" + playerData.getPlayerName() + " &edoesn't have an active cooldown with key &6" + key);

            final var duration = explorer.parse(argDuration);
            info.reduceFlat(duration);
            return explorer.success("Player &6" + playerData.getPlayerName() + "&e's cooldown with key &6" + key + "&e reduced to &6" + (info.getRemaining() / 1000d) + " sec");
        }
    }

    static class Percent extends AbstractReduce {
        private final Argument<@NotNull Double> argPercent;
        private final BiConsumer<CooldownInfo, Double> consumer;

        public Percent(@NotNull CommandTreeNode parent, String name, BiConsumer<CooldownInfo, Double> consumer) {
            super(parent, name);

            argPercent = addArgument(Argument.AMOUNT_DOUBLE);
            this.consumer = consumer;
        }

        @Override
        public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
            final var key = explorer.parse(argKey);
            final var playerData = MMOPlayerData.get(explorer.parse(argPlayer));
            final var percent = explorer.parse(argPercent);

            final var info = playerData.getCooldownMap().getInfo(key);
            if (info == null || info.hasEnded())
                return explorer.success("Player &6" + playerData.getPlayerName() + " &edoesn't have an active cooldown with key &6" + key);

            consumer.accept(info, percent);
            return explorer.success("Player &6" + playerData.getPlayerName() + "&e's cooldown with key &6" + key + "&e reduced to &6" + (info.getRemaining() / 1000d) + " sec");
        }
    }
}