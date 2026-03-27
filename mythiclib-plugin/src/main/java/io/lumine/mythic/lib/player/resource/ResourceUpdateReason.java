package io.lumine.mythic.lib.player.resource;

public enum ResourceUpdateReason {

    /**
     * When resource is being regenerated
     */
    REGENERATION,

    /**
     * When some resource is regenerated or consumed by some skill
     */
    SKILL,

    /**
     * When consuming stellium to use a waypoint (MMOCore)
     */
    WAYPOINT,

    /**
     * When the player chooses a class, all their MMOCore resources
     * including health, mana... from their previous game session
     * is restored. This update reason is typically not gameplay.
     */
    CHOOSE_CLASS(false),

    /**
     * When some resource is regenerated or consumed by an item
     * such as a MMOItems consumable
     */
    ITEM,

    /**
     * Default reason used by MMOCore quest triggers and MMOLib script mechanics
     *
     * @see io.lumine.mythic.lib.script.mechanic.buff.HealMechanic
     */
    MECHANIC,

    /**
     * When a player's "Max Resource" stat decreases so that the player's
     * current resource value needs to be brought down to avoid exceeding the
     * new max resource value, the player's current resource value gets updated
     * using this reason.
     */
    CLAMPING(false),

    /**
     * When using the MMOCore resource command
     */
    COMMAND,

    /**
     * Anything else, where the reason is not provided by
     * the user or no previous reason applies
     */
    OTHER;

    private final boolean callEvent;

    ResourceUpdateReason() {
        this(true);
    }

    ResourceUpdateReason(boolean callEvent) {
        this.callEvent = callEvent;
    }

    public boolean callsEvent() {
        return callEvent;
    }

    public boolean isRegeneration() {
        return this == REGENERATION || this == SKILL;
    }
}
