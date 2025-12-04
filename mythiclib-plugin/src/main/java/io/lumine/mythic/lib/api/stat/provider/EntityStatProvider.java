package io.lumine.mythic.lib.api.stat.provider;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * This class can be used to cache the equipment from a mob and read its
 * statistics like Defense, Elemental defense or mitigation stats.
 * <p>
 * This way, mechanics which were initially limited to players can now be
 * generalized to any monster
 * <p>
 * TODO Implement the following mechanics so that they also work on monsters
 * - Damage mitigation
 * - Elemental mechanics
 * - Critical strikes
 *
 * @author indyuce
 */
public class EntityStatProvider implements StatProvider {
    private final Set<NBTItem> equipment = new HashSet<>();
    private final LivingEntity entity;

    public EntityStatProvider(LivingEntity entity) {
        this.entity = entity;

        // ModelEngine mobs have no equipment TODO support
        final @Nullable EntityEquipment equip = entity.getEquipment();
        if (equip == null)
            return;

        for (ItemStack equipped : entity.getEquipment().getArmorContents())
            registerItem(equipped);
        registerItem(entity.getEquipment().getItemInMainHand());
        registerItem(entity.getEquipment().getItemInOffHand());
    }

    @Override
    public @NotNull EquipmentSlot getActionHand() {
        return EquipmentSlot.MAIN_HAND;
    }

    @Override
    public @NotNull LivingEntity getEntity() {
        return entity;
    }

    private void registerItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta())
            return;

        equipment.add(NBTItem.get(item));
    }

    @Override
    public double getStat(String id) {
        double d = 0;

        for (NBTItem nbt : equipment)
            d += nbt.getStat(id);

        return d;
    }
}
