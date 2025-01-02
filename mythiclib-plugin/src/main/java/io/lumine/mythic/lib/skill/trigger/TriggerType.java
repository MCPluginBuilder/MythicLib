package io.lumine.mythic.lib.skill.trigger;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.handler.def.passive.Backstab;
import io.lumine.mythic.lib.util.annotation.NotUsed;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TriggerType {

    // Combat

    /**
     * Called when a player kills an entity
     * Metadata target is the entity dying
     */
    @NotNull
    public static TriggerType KILL_ENTITY = new TriggerType("KILL_ENTITY"),

    /**
     * This is a sort-of duplicate of the {@link #KILL_ENTITY} trigger type
     * which only triggers on players instead of any type of entity.
     */
    KILL_PLAYER = new TriggerType("KILL_PLAYER"),

    /**
     * Called when a player attacks any other entity
     * Metadata target is the entity being attacked
     */
    ATTACK = new TriggerType("ATTACK", true, true, true),

    /**
     * Called when a player is damaged by any source
     */
    DAMAGED = new TriggerType("DAMAGED"),

    /**
     * Called when a player is damaged by an entity (explosion/attack)
     * Skill target is the entity damaging the player
     */
    DAMAGED_BY_ENTITY = new TriggerType("DAMAGED_BY_ENTITY"),

    /**
     * Called when a player dies
     */
    DEATH = new TriggerType("DEATH"),

    // Blocks

    /**
     * Called when a player places a block.
     */
    PLACE_BLOCK = new TriggerType("PLACE_BLOCK"),

    /**
     * Called when a player breaks a block.
     */
    BREAK_BLOCK = new TriggerType("BREAK_BLOCK"),

    // Bow or crossbow

    /**
     * Called when an arrow is shot from a bow
     * Skill target is the arrow fired
     */
    SHOOT_BOW = new TriggerType("SHOOT_BOW", true, true, true),

    /**
     * Called every tick when an arrow flies in the air
     * Skill target is the arrow fired
     */
    ARROW_TICK = new TriggerType("ARROW_TICK", true, true, true),

    /**
     * Called when an arrow hits an entity
     * Skill target is the entity hit (the arrow dies right afterwards)
     */
    ARROW_HIT = new TriggerType("ARROW_HIT", true, true, true),

    /**
     * Called when an arrow hits a block
     * Skill target is the arrow fired
     */
    ARROW_LAND = new TriggerType("ARROW_LAND", true, true, true),

    // Trident

    /**
     * Called when a player shoots a trident
     * Skill target is the thrown trident
     */
    SHOOT_TRIDENT = new TriggerType("SHOOT_TRIDENT", true, true, true),

    /**
     * Called every tick when a trident flies in the air
     * Skill target is the thrown trident
     */
    TRIDENT_TICK = new TriggerType("TRIDENT_TICK", true, true, true),

    /**
     * Called when an arrow hits an entity
     * Skill target is the thrown trident
     */
    TRIDENT_HIT = new TriggerType("TRIDENT_HIT", true, true, true),

    /**
     * Called when a thrown trident hits a block
     * Skill target is the trident thrown
     */
    TRIDENT_LAND = new TriggerType("TRIDENT_LAND", true, true, true),

    // Clicks

    /**
     * Called when a player right clicks
     * This trigger displays cooldown/mana restriction messages.
     */
    RIGHT_CLICK = new TriggerType("RIGHT_CLICK", false, true, true),

    /**
     * Called when a player left clicks
     * This trigger displays cooldown/mana restriction messages.
     */
    LEFT_CLICK = new TriggerType("LEFT_CLICK", false, true, true),

    /**
     * Called when a player right clicks while crouching
     * This trigger displays cooldown/mana restriction messages.
     */
    SHIFT_RIGHT_CLICK = new TriggerType("SHIFT_RIGHT_CLICK", false, true, true),

    /**
     * Called when a player left clicks while crouching
     * This trigger displays cooldown/mana restriction messages.
     */
    SHIFT_LEFT_CLICK = new TriggerType("SHIFT_LEFT_CLICK", false, true, true),

    /**
     * Called when a player drops an item by pressing Q while crouching
     */
    DROP_ITEM = new TriggerType("DROP_ITEM", false),

    /**
     * Called when a player drops an item by pressing Q while crouching
     */
    SHIFT_DROP_ITEM = new TriggerType("SHIFT_DROP_ITEM", false),

    /**
     * Called when a player swaps mainhand/offhand items
     */
    SWAP_ITEMS = new TriggerType("SWAP_ITEMS", false),

    /**
     * Called when a player swaps mainhand/offhand items while crouching
     */
    SHIFT_SWAP_ITEMS = new TriggerType("SHIFT_SWAP_ITEMS", false),

    // Misc

    /**
     * Called when a player logs in
     */
    LOGIN = new TriggerType("LOGIN"),

    /**
     * Called when a player toggles on or off sneaking.
     * This trigger displays cooldown/mana restriction messages.
     */
    SNEAK = new TriggerType("SNEAK", false),

    /**
     * Called when a player teleports from one location to another.
     * Locations can have different worlds.
     * <p>
     * !! WARNING !! Skill target location is the location of the player
     * before teleportation, and skill source is the current player's location.
     */
    TELEPORT = new TriggerType("TELEPORT"),

    /**
     * Casts the skill at regular time intervals. Timer period
     * can be edited using the corresponding skill parameter.
     */
    TIMER = new TriggerType("TIMER"),

    /**
     * Used when a player actively casts a skill (e.g MMOCore
     * skill casting system). This is the only trigger type
     * which you can use to create an active skill. Any other
     * trigger type is used for passive skills.
     */
    CAST = new TriggerType("CAST", false, false),

    /**
     * Should be used by plugins when passive skills get triggered by
     * another cause not listed in {@link TriggerType}. This trigger
     * type is used by any hard coded passive skills like {@link Backstab}.
     *
     * @see {@link SkillHandler#isTriggerable()}
     */
    API = new TriggerType("API");

    /**
     * Called when a player equips armor
     */
    @NotUsed
    public static final TriggerType EQUIP_ARMOR = new TriggerType("EQUIP_ARMOR", false),

    /**
     * Called when a player unequips armor
     */
    UNEQUIP_ARMOR = new TriggerType("UNEQUIP_ARMOR", false);

    // CONSUME, POTION_SPLASH, PICKUP, UNCROUCH, INTERACT

    private static final Map<String, TriggerType> BY_ID = new HashMap<>();

    static {
        register(KILL_ENTITY);
        register(KILL_PLAYER);
        register(ATTACK);
        register(DAMAGED);
        register(DAMAGED_BY_ENTITY);
        register(DEATH);

        register(PLACE_BLOCK);
        register(BREAK_BLOCK);

        register(SHOOT_BOW);
        register(ARROW_TICK);
        register(ARROW_HIT);
        register(ARROW_LAND);

        register(SHOOT_TRIDENT);
        register(TRIDENT_TICK);
        register(TRIDENT_HIT);
        register(TRIDENT_LAND);

        register(RIGHT_CLICK);
        register(LEFT_CLICK);
        register(DROP_ITEM);
        register(SWAP_ITEMS);
        register(SHIFT_RIGHT_CLICK);
        register(SHIFT_LEFT_CLICK);
        register(SHIFT_DROP_ITEM);
        register(SHIFT_SWAP_ITEMS);

        register(LOGIN);
        register(SNEAK);
        register(TELEPORT);
        //register(EQUIP_ARMOR);
        //register(UNEQUIP_ARMOR);
        register(TIMER);
        register(CAST);
        register(API);
    }

    private final String id;
    private final boolean silent, passive, actionHandSpecific;

    public TriggerType(String id) {
        this(id, true, true);
    }

    public TriggerType(String id, boolean silent) {
        this(id, silent, true);
    }

    /**
     * This constructor is made private to make sure there is only
     * one trigger type that generates active skills.
     *
     * @param id      The trigger type ID
     * @param silent  Does this trigger type generate silent skills
     * @param passive Does this trigger type generate passive skills
     */
    private TriggerType(String id, boolean silent, boolean passive) {
        this(id, silent, passive, false);
    }

    /**
     * This constructor is made private to make sure there is only
     * one trigger type that generates active skills.
     *
     * @param id      The trigger type ID
     * @param silent  Does this trigger type generate silent skills
     * @param passive Does this trigger type generate passive skills
     */
    private TriggerType(String id, boolean silent, boolean passive, boolean actionHandSpecific) {
        this.id = id;
        this.silent = silent;
        this.passive = passive;
        this.actionHandSpecific = actionHandSpecific;
    }

    /**
     * @return Identical to {@link #toString()}
     */
    @NotNull
    public String name() {
        return id;
    }

    /**
     * When set to false, any skill with this trigger type should
     * send a message to the player if this skill cannot be used
     * i.e if the caster doesn't have enough mana, if it's on
     * cooldown...
     */
    public boolean isSilent() {
        return silent;
    }

    /**
     * When set to true, skills granted by the item held in the
     * secondary hand (opposite of the action hand) will not be applied.
     * <p>
     * These triggers correspond to item interactions (clicks, attacks).
     */
    public boolean isActionHandSpecific() {
        return actionHandSpecific;
    }

    /**
     * There are two types of passive skills:
     * - hard coded passive skills which use the API trigger type
     * - passive skills created using other skill plugins using
     * any other trigger type apart from the CAST trigger type
     *
     * @return If this skill is passive
     */
    public boolean isPassive() {
        return passive;
    }

    public String getName() {
        return UtilityMethods.caseOnWords(name().toLowerCase().replace("_", " "));
    }

    public String getLowerCaseId() {
        return name().toLowerCase().replace("_", "-");
    }

    /**
     * @return This trigger type serialized into a string
     */
    @Override
    public String toString() {
        return name();
    }

    /**
     * @param id The string trying to convert
     * @return The trigger type of this name
     * @throws IllegalArgumentException If the string does not correspond to a trigger type
     */
    @NotNull
    public static TriggerType valueOf(String id) {
        return Objects.requireNonNull(BY_ID.get(id), "Could not find trigger type with ID '" + id + "'");
    }

    public static void register(@NotNull TriggerType trigger) {
        Validate.notNull(trigger, "Trigger type cannot be null");
        BY_ID.put(trigger.name(), trigger);
    }

    public static Collection<TriggerType> values() {
        return BY_ID.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TriggerType that = (TriggerType) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
