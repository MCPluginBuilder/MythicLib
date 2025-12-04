package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import org.bukkit.Color;
import org.bukkit.Location;
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
    ITEM_SNOWBALL("SNOWFLAKE", "SNOW_SHOVEL"),
    ENTITY_EFFECT("ENTITY_EFFECT", "SPELL_MOB"),
    ENTITY_EFFECT_AMBIENT("ENTITY_EFFECT", "SPELL_MOB_AMBIENT"),
    TOTEM_OF_UNDYING("TOTEM_OF_UNDYING", "TOTEM"),
    HAPPY_VILLAGER("HAPPY_VILLAGER", "VILLAGER_HAPPY"),
    SNOWFLAKE("ITEM_SNOWBALL", "SNOWBALL"),
    BLOCK("BLOCK", "BLOCK_CRACK"),
    BLOCK_DUST("BLOCK", "BLOCK_DUST"),
    ITEM_SLIME("ITEM_SLIME", "SLIME"),
    ENCHANTED_HIT("ENCHANTED_HIT", "CRIT_MAGIC"),
    ITEM("ITEM", "ITEM_CRACK"),
    ;

    // Cache what these particle effects need
    public final boolean /*dustOptions, blockData,*/ spell;

    private final Particle wrapped;

    VParticle(String... candidates) {
        wrapped = UtilityMethods.resolveField(Particle::valueOf, candidates);

        //this.dustOptions = wrapped.getDataType() == Particle.DustOptions.class;
        //this.blockData = wrapped.getDataType() == BlockData.class;
        this.spell = MythicLib.plugin.getVersion().isAbove(1, 21, 9) && wrapped.getDataType() == Particle.Spell.class;
    }

    @NotNull
    public Particle get() {
        return wrapped;
    }

    public void spawnSafeSpell(Location loc) {
        if (!this.spell) spawnWithoutData(loc);
        else spawnWithData(loc, new Particle.Spell(Color.WHITE, 1));
    }

    public void spawnSafeSpell(Location loc, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (!this.spell) spawnWithoutData(loc, count, offsetX, offsetY, offsetZ, speed);
        else spawnWithData(loc, count, offsetX, offsetY, offsetZ, speed, new Particle.Spell(Color.WHITE, 1));
    }

    /*
    public void spawn(Location loc, Particle.Spell spell) {
        spawnWithData(loc, spell);
    }

    public void spawn(Location loc, int count, float offsetX, float offsetY, float offsetZ, float speed, Particle.Spell spell) {
        if (!this.spell) spawnWithoutData(loc, count, offsetX, offsetY, offsetZ, speed);
        else spawnWithData(loc, count, offsetX, offsetY, offsetZ, speed, spell);
    }
    */

    private static final double DEF_OFFSET = 0d;
    private static final double DEF_SPEED = 0d;
    private static final int DEF_COUNT = 1;

    private void spawnWithoutData(Location loc) {
        loc.getWorld().spawnParticle(wrapped, loc, DEF_COUNT, DEF_OFFSET, DEF_OFFSET, DEF_OFFSET, DEF_SPEED);
    }

    private void spawnWithoutData(Location loc, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        loc.getWorld().spawnParticle(wrapped, loc, count, offsetX, offsetY, offsetZ, speed);
    }

    private void spawnWithData(Location loc, Object data) {
        loc.getWorld().spawnParticle(wrapped, loc, DEF_COUNT, DEF_OFFSET, DEF_OFFSET, DEF_OFFSET, DEF_SPEED, data);
    }

    private void spawnWithData(Location loc, int count, double offsetX, double offsetY, double offsetZ, double speed, Object data) {
        loc.getWorld().spawnParticle(wrapped, loc, count, offsetX, offsetY, offsetZ, speed, data);
    }
}