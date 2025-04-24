package io.lumine.mythic.lib.api.player;

import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.lang3.NotImplementedException;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Used by MythicLib to make a difference between stat
 * modifiers granted by offhand and mainhand items.
 * <p>
 * Used by the MMOItems player inventory manager to differentiate
 * where items were placed.
 *
 * @author indyuce
 */
public enum EquipmentSlot {

    @Deprecated
    ARMOR(true, false, null),

    /**
     * When placed in head slot
     */
    HEAD(true, false, org.bukkit.inventory.EquipmentSlot.HEAD),

    /**
     * When placed in head slot
     */
    CHEST(true, false, org.bukkit.inventory.EquipmentSlot.CHEST),

    /**
     * When placed in head slot
     */
    LEGS(true, false, org.bukkit.inventory.EquipmentSlot.LEGS),

    /**
     * When placed in head slot
     */
    FEET(true, false, org.bukkit.inventory.EquipmentSlot.FEET),

    /**
     * When placed in an accessory slot.
     */
    ACCESSORY(false, false, null),

    /**
     * When placed somewhere inside the player's inventory. It
     * is primarily used for MMOItems ornaments so that items anywhere
     * in the player's inventory may apply their stats.
     *
     * @see ModifierSource#ORNAMENT
     */
    INVENTORY(false, false, null),

    /**
     * When placed in main hand.
     */
    MAIN_HAND(false, true, org.bukkit.inventory.EquipmentSlot.HAND),

    /**
     * When placed in off hand.
     */
    OFF_HAND(false, true, org.bukkit.inventory.EquipmentSlot.OFF_HAND),

    /**
     * Fictive equipment slot which overrides all
     * rules and apply the item stats whatsoever.
     */
    OTHER(false, false, null);

    private final boolean body, hand;
    private final org.bukkit.inventory.EquipmentSlot bukkit;

    EquipmentSlot(boolean body, boolean hand, org.bukkit.inventory.EquipmentSlot bukkit) {
        this.body = body;
        this.hand = hand;
        this.bukkit = bukkit;
    }

    public boolean isBody() {
        return body;
    }

    public boolean isHand() {
        return hand;
    }

    @NotNull
    public org.bukkit.inventory.EquipmentSlot toBukkit() {
        return Objects.requireNonNull(bukkit, "No Bukkit equivalent");
    }

    @NotNull
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
     *         be taken into account given the action hand
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
                return equipmentSlot.body;
            case ACCESSORY:
                return equipmentSlot == ACCESSORY;
            case ORNAMENT:
                return equipmentSlot == INVENTORY;

            default:
                throw new NotImplementedException();
        }
    }

    @NotNull
    public static EquipmentSlot fromBukkit(org.bukkit.inventory.EquipmentSlot slot) {
        for (EquipmentSlot checked : values())
            if (checked.bukkit == slot)
                return checked;
        return OTHER;
    }
}