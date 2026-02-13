package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EquipCommand extends CommandTreeNode {
    public EquipCommand(CommandTreeNode parent) {
        super(parent, "equip-facing");
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        Validate.isTrue(sender instanceof Player, "Only players can use this command");

        final var player = (Player) sender;

        // Find item to equip
        final ItemStack item;
        final EquipmentSlot itemHand;
        if (!UtilityMethods.isAir(player.getInventory().getItemInMainHand())) {
            item = player.getInventory().getItemInMainHand();
            itemHand = EquipmentSlot.HAND;
        } else if (!UtilityMethods.isAir(player.getInventory().getItemInOffHand())) {
            item = player.getInventory().getItemInOffHand();
            itemHand = EquipmentSlot.OFF_HAND;
        } else return explorer.fail("No item in hand to equip");

        // Find entity to equip
        final var result = player.getWorld().rayTrace(player.getEyeLocation(), player.getEyeLocation().getDirection(), 10, FluidCollisionMode.NEVER, false, 0, ent -> !ent.equals(player));
        final var hitEntity = result.getHitEntity();
        if (hitEntity == null) return explorer.fail("No entity in sight");
        if (!(hitEntity instanceof LivingEntity)) return explorer.fail("Target is not a living entity");

        final var slot = item.getType().getEquipmentSlot();
        final var previousEquipped = ((LivingEntity) hitEntity).getEquipment().getItem(slot);
        ((LivingEntity) hitEntity).getEquipment().setItem(slot, item);
        player.getInventory().setItem(itemHand, previousEquipped);

        return explorer.success("Equipped " + item.getType().name() + " to " + hitEntity.getName() + "'s slot " + slot.name());
    }
}
