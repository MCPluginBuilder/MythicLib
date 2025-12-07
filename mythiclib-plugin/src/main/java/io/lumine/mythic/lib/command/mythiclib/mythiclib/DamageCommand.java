package io.lumine.mythic.lib.command.mythiclib.mythiclib;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DamageCommand extends CommandTreeNode {
    private final Argument<Player> argPlayer;
    private final Argument<LivingEntity> argTarget;
    private final Argument<Integer> argValue;

    public DamageCommand(CommandTreeNode parent) {
        super(parent, "damage");

        argPlayer = addArgument(Argument.PLAYER.withKey("damager"));
        argTarget = addArgument(Argument.LIVING_ENTITY.withKey("target"));
        argValue = addArgument(Argument.AMOUNT_INT);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        final var player = explorer.parse(argPlayer);
        final var target = explorer.parse(argTarget);

        // Find value
        final double value = explorer.parse(argValue);

        // Register attack
        final var playerData = MMOPlayerData.get(player.getUniqueId()).getStatMap().cache(EquipmentSlot.MAIN_HAND);
        final var damage = new DamageMetadata(value, List.of()); // TODO damage types & elements
        final var attack = new AttackMetadata(damage, target, playerData);
        MythicLib.plugin.getDamage().registerAttack(attack);

        return CommandResult.SUCCESS;
    }
}
