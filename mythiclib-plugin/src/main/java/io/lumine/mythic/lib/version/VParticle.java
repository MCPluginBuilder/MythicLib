package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.UtilityMethods;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public enum VParticle {
    EXPLOSION("POOF", "EXPLOSION_NORMAL"),
    LARGE_EXPLOSION("EXPLOSION", "EXPLOSION_LARGE"), // EXPLOSION_EMITTER is a huge explosion
    WITCH("WITCH", "SPELL_WITCH"),
    LARGE_SMOKE("LARGE_SMOKE", "SMOKE_LARGE"),
    SMOKE("SMOKE", "SMOKE_NORMAL"),
    REDSTONE("DUST", "REDSTONE"),
    FIREWORK("FIREWORK", "FIREWORKS_SPARK"),
    INSTANT_EFFECT("INSTANT_EFFECT", "SPELL_INSTANT"),
    EFFECT("EFFECT", "SPELL"),
    /**
     * Requires color
     */
    ENTITY_EFFECT("ENTITY_EFFECT", "SPELL_MOB"),
    ENTITY_EFFECT_AMBIENT("ENTITY_EFFECT", "SPELL_MOB_AMBIENT"),
    TOTEM_OF_UNDYING("TOTEM_OF_UNDYING", "TOTEM"),
    HAPPY_VILLAGER("HAPPY_VILLAGER", "VILLAGER_HAPPY"),
    SNOWFLAKE("ITEM_SNOWBALL", "SNOWBALL"),
    BLOCK("BLOCK", "BLOCK_CRACK"),
    /**
     * Requires material
     */
    BLOCK_DUST("BLOCK", "BLOCK_DUST"),
    ITEM_SNOWBALL("SNOWFLAKE", "SNOW_SHOVEL"),
    ITEM_SLIME("ITEM_SLIME", "SLIME"),
    ENCHANTED_HIT("ENCHANTED_HIT", "CRIT_MAGIC"),
    ITEM("ITEM", "ITEM_CRACK"),

    ;

    private final Particle wrapped;

    VParticle(String... candidates) {
        wrapped = UtilityMethods.resolveField(Particle::valueOf, candidates);
    }

    @NotNull
    public Particle get() {
        return wrapped;
    }
}