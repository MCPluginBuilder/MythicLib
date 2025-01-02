package io.lumine.mythic.lib.api.player;

import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.util.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

/**
 * Used by MythicLib to make a difference between stat
 * modifiers granted by off hand and main hand items.
 * <p>
 * Used by MMOItems player inventory updates to differentiate where
 * items were placed in the player inventory.
 *
 * @author indyuce
 */
public enum EquipmentSlot {

    /**
     * When placed in any armor slot. No distinction between
     * helmet, chestplate, leggings and boots unlike vanilla
     * Minecraft since you can't place a chestplate item
     * inside of the feet slot for instance.
     */
    ARMOR,

    /**
     * When placed in an accessory slot.
     */
    ACCESSORY,

    /**
     * When placed in main hand.
     */
    MAIN_HAND,

    /**
     * When placed in off hand.
     */
    OFF_HAND,

    /**
     * Fictive equipment slot which overrides all
     * rules and apply the item stats whatsoever.
     */
    OTHER;

    @NotNull
    public org.bukkit.inventory.EquipmentSlot toBukkit() {
        switch (this) {
            case MAIN_HAND:
                return org.bukkit.inventory.EquipmentSlot.HAND;
            case OFF_HAND:
                return org.bukkit.inventory.EquipmentSlot.OFF_HAND;
            default:
                throw new RuntimeException("Not a hand slot");
        }
    }

    private EquipmentSlot getOppositeHand() {
        Validate.isTrue(this == MAIN_HAND || this == OFF_HAND, "Not a hand equipment slot");
        return this == MAIN_HAND ? OFF_HAND : MAIN_HAND;
    }

    /**
     * Basic modifier application rule.
     *
     * @param modifier Player modifier
     * @return If a modifier should be taken into account given the action hand
     */
    public boolean isCompatible(PlayerModifier modifier) {
        return isCompatible(modifier.getSource(), modifier.getSlot());
    }

    /**
     * Every action has a player HAND associated to it, called the action hand.
     * It corresponds to the hand the the player is using to perform an action.
     * By default, MythicLib uses the Main hand if none is specified.
     * The action hand is the enum value calling this method.
     * <p>
     * Modifiers from both hands are registered in modifier maps YET filtered out when
     * calculating stat values/filtering out abilities/... Modifiers from the other hand
     * are ignored IF AND ONLY IF the other hand item is a weapon. As long as the item
     * placement is valid, non-weapon items all apply their modifiers.
     * <p>
     * Filtering out the right player modifiers is referred as "isolating modifiers"
     *
     * @param modifierSource Source of modifier
     * @param equipmentSlot  Equipment slot of modifier
     * @return If a modifier with the given equipment slot and modifier source should
     * be taken into account given the action hand
     */
    public boolean isCompatible(@NotNull ModifierSource modifierSource, @NotNull EquipmentSlot equipmentSlot) {
        Validate.isTrue(isHand(), "Instance called must be a hand equipment slot");
        if (equipmentSlot == OTHER) return true;

        switch (modifierSource) {

            // Simple rules
            case VOID:
                return false;
            case OTHER:
                return true;

            // Ignore modifiers from opposite hand if it's a weapon
            case RANGED_WEAPON:
            case MELEE_WEAPON:
                return equipmentSlot == this;

            // Hand items
            case OFFHAND_ITEM:
                return equipmentSlot == OFF_HAND;
            case MAINHAND_ITEM:
                return equipmentSlot == MAIN_HAND;
            case HAND_ITEM:
                return equipmentSlot.isHand();

            // Accessories & armor
            case ARMOR:
                return equipmentSlot == ARMOR;
            case ACCESSORY:
                return equipmentSlot == ACCESSORY;

            default:
                throw new NotImplementedException();
        }
    }

    public boolean isHand() {
        return this == MAIN_HAND || this == OFF_HAND;
    }

    public static EquipmentSlot fromBukkit(org.bukkit.inventory.EquipmentSlot slot) {
        switch (slot) {
            case HAND:
                return MAIN_HAND;
            case OFF_HAND:
                return OFF_HAND;
            case FEET:
            case HEAD:
            case LEGS:
            case CHEST:
                return ARMOR;
            default:
                return OTHER;
        }
    }
}