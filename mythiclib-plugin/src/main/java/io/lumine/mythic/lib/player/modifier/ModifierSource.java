package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;

/**
 * Main problem solved by the modifier source is being able to
 * calculate specific statistics while ignoring other modifiers.
 * When calculating the player's attack damage when using a main
 * hand weapon, MMOItems must completely ignore attack damage
 * given by off-hand modifiers.
 *
 * @author indyuce
 * @see EquipmentSlot#isCompatible(PlayerModifier)
 */
public enum ModifierSource {

    /**
     * Modifier given by a melee weapon. These modifiers should only be
     * taken into account when the player wears the item in the main hand.
     */
    MELEE_WEAPON,

    /**
     * Modifier given by a ranged weapon. These modifiers should only be
     * taken into account when the player wears the item in the main hand.
     * <p>
     * Ranged weapons are handled separately in MMOItems as MythicLib must
     * not add specific attribute modifiers, including Atk Damage and Speed.
     */
    RANGED_WEAPON,

    /**
     * Modifier given by an offhand item. These modifiers should only be
     * taken into account when the player wears the item in the offhand.
     */
    OFFHAND_ITEM,

    /**
     * Modifier given by a mainhand item. These modifiers should only be
     * taken into account when the player wears the item in the mainhand.
     */
    MAINHAND_ITEM,

    /**
     * Modifier given by a hand item. These modifiers should only be
     * taken into account when the player holds the item in one of their hands.
     */
    HAND_ITEM,

    /**
     * Modifier given by an armor item. Modifiers are applied if
     * worn in an armor slot.
     */
    ARMOR,

    /**
     * Modifier given by an accessory. Modifiers are applied if worn
     * in an accessory slot.
     */
    ACCESSORY,

    /**
     * Modifier source given by MMOItems ornaments, nothing else.
     * Only applies stats if placed inside the inventory. Hand or
     * armor slots do not work.
     *
     * @see EquipmentSlot#INVENTORY
     */
    ORNAMENT,

    /**
     * Modifier given by anything else. Modifiers always apply.
     * <p>
     * Has a lower priority compared to {@link EquipmentSlot#OTHER}
     */
    OTHER,

    /**
     * Modifiers never apply whatsoever.
     * <p>
     * Has a lower priority compared to {@link EquipmentSlot#OTHER}
     */
    VOID;

    /**
     * @return Is the source of the modifier a weapon?
     */
    public boolean isWeapon() {
        return this == MELEE_WEAPON || this == RANGED_WEAPON;
    }

    /**
     * @return Is the source of the modifier an item that must be handheld (accessories & weapons).
     */
    public boolean isHandheld() {
        return isEquipment() && this != ARMOR && this != ACCESSORY;
    }

    /**
     * @return Is the source of the modifier a piece of equipment (either handheld or armor).
     */
    public boolean isEquipment() {
        return this != VOID && this != OTHER;
    }
}
