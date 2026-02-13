package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.TemporaryStatModifier;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeRoot;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Deprecated
public class MMOTempStatCommand extends CommandTreeRoot {
    public MMOTempStatCommand(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        explorer.verbose("/mmotempstat is deprecated. Use instead /ml tempstat ...");

        if (args.length < 4) {
            return explorer.fail("&cNot enough args. Usage: /mmotempstat <player> <stat name> <value> <tick duration>");
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) return explorer.fail("Player not found");

        MMOPlayerData playerData = MMOPlayerData.get(target);

        Pair<ModifierType, Double> modifierPair = ModifierType.pairFromString(args[2]);
        ModifierType type = modifierPair.getLeft();
        double value = modifierPair.getRight();
        long duration = Long.parseLong(args[3]);

        new TemporaryStatModifier(UUID.randomUUID().toString(), args[1], value, type, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData, duration);

        return CommandResult.SUCCESS;
    }
}
