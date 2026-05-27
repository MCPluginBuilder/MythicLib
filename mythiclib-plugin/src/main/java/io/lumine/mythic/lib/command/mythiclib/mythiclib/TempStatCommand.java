package io.lumine.mythic.lib.command.mythiclib.mythiclib;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.api.stat.modifier.TemporaryStatModifier;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.command.argument.ArgumentParseException;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.Pair;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Deprecated
public class TempStatCommand extends CommandTreeNode {

    @Deprecated
    public TempStatCommand(@NotNull CommandTreeNode parent) {
        super(parent, "tempstat");

        addChild(new AddCommand(this));
        addChild(new RemoveCommand(this));
    }

    @Deprecated
    private static class RemoveCommand extends CommandTreeNode {
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

    @Deprecated
    private static class AddCommand extends CommandTreeNode {
        private final Argument<@NotNull Player> argPlayer;
        private final Argument<@NotNull String> argStat;
        private final Argument<@NotNull Pair<ModifierType, Double>> argValue;
        private final Argument<@NotNull Long> argDuration;
        private final Argument<@NotNull String> argKey;

        @Deprecated
        public AddCommand(@NotNull CommandTreeNode parent) {
            super(parent, "add");

            argPlayer = addArgument(Argument.PLAYER);
            argStat = addArgument(Argument.STAT);
            argValue = addArgument(VALUE);
            argDuration = addArgument(Argument.DURATION_TICKS.withFallback(explorer -> 0L));
            argKey = addArgument(Argument.MODIFIER_KEY);
        }

        public static final Argument<@NotNull Pair<ModifierType, Double>> VALUE = new Argument<>("value",
                (explorer, completions) -> completions.addAll(Arrays.asList("0.1", "0.5", "2")),
                (explorer, input) -> {
                    try {
                        return ModifierType.pairFromString(input);
                    } catch (Exception exception) {
                        throw new ArgumentParseException("Invalid stat modifier value", exception);
                    }
                });

        @Override
        public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
            final var target = explorer.parse(argPlayer);
            final var statName = explorer.parse(argStat);

            final var rawValue = explorer.parse(argValue);
            final var type = rawValue.getLeft();
            final var value = rawValue.getRight();

            final long duration = explorer.parse(argDuration);
            final var key = explorer.parse(argKey);

            final var playerData = MMOPlayerData.get(target);

            // Permanent modifier
            if (duration <= 0) {
                new StatModifier(key, statName, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData);
            }

            // Temporary modifier
            else {
                new TemporaryStatModifier(key, statName, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData, duration);
            }

            return explorer.success("Modifier of &6" + value + type.toStringSuffix() + " " + statName + "&e for &6" + duration + "&e ticks given to &6" + target.getName());
        }
    }
}
