package io.lumine.mythic.lib.player.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierSupplier;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class EquippedItem implements ModifierSupplier {
    protected final EquipmentSlot equipmentSlot;
    protected final int slotIndex, watcherId;

    protected final List<PlayerModifier> modifierCache = new ArrayList<>();

    /**
     * Item placed inside a player vanilla slot (helmet, offhand...)
     *
     * @param equipmentSlot Equipment slot
     */
    public EquippedItem(EquipmentSlot equipmentSlot) {
        this(equipmentSlot, 0, 0);
    }

    /**
     * Item placed inside the vanilla player inventory
     *
     * @param slotIndex Physical location
     */
    public EquippedItem(int slotIndex) {
        this(EquipmentSlot.INVENTORY, slotIndex, 0);
    }

    /**
     * Item placed inside a custom accessory inventory
     *
     * @param slotIndex Physical location
     * @param watcherId Unique ID of inventory
     */
    public EquippedItem(int slotIndex, int watcherId) {
        this(EquipmentSlot.ACCESSORY, slotIndex, watcherId);
    }

    /**
     * Generic constructor
     *
     * @param equipmentSlot      Equipment slot, used for modifier resolution
     * @param slotIndex Index of slot (position of item in the inventory, if any)
     * @param watcherId If of custom inventory, if any
     */
    public EquippedItem(EquipmentSlot equipmentSlot, int slotIndex, int watcherId) {
        this.equipmentSlot = equipmentSlot;
        this.slotIndex = slotIndex;
        this.watcherId = watcherId;
    }

    @NotNull
    @Override
    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public int getWatcherId() {
        return watcherId;
    }

    @NotNull
    @Override
    public List<PlayerModifier> getModifierCache() {
        return modifierCache;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        EquippedItem that = (EquippedItem) object;
        return slotIndex == that.slotIndex && watcherId == that.watcherId && equipmentSlot == that.equipmentSlot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentSlot, slotIndex, watcherId);
    }
}
