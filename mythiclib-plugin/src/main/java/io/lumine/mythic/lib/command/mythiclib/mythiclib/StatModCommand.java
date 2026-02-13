package io.lumine.mythic.lib.command.mythiclib.mythiclib;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.api.stat.modifier.TemporaryStatModifier;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Deprecated
public class StatModCommand extends CommandTreeNode {

    @Deprecated
    public StatModCommand(CommandTreeNode parent) {
        super(parent, "statmod");

        addArgument(Argument.PLAYER);
        addArgument(Argument.STAT);
        addArgument(Argument.AMOUNT_DOUBLE);
        addArgument(Argument.DURATION_TICKS.withFallback(explorer -> 0L));
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        explorer.verbose("/ml statmod is deprecated, use '/ml stat' instead");
        if (args.length < 4) return CommandResult.THROW_USAGE;

        final @Nullable Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            return explorer.fail("Player not found");
        }

        final String statName = UtilityMethods.enumName(args[2]);
        final MMOPlayerData playerData = MMOPlayerData.get(target);
        Pair<ModifierType, Double> modifierPair = ModifierType.pairFromString(args[3]);
        final ModifierType type = modifierPair.getLeft();
        final double value = modifierPair.getRight();
        final long duration = args.length > 4 ? Math.max(1, (long) Double.parseDouble(args[4])) : 0;

        if (duration <= 0)
            new StatModifier(UUID.randomUUID().toString(), statName, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData);
        else
            new TemporaryStatModifier(UUID.randomUUID().toString(), statName, value, type, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData, duration);
        return CommandResult.SUCCESS;
    }
}
