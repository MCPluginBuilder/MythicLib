package io.lumine.mythic.lib.api.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.api.stat.provider.PlayerStatProvider;
import io.lumine.mythic.lib.player.PlayerMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatMap implements PlayerStatProvider {
    private final MMOPlayerData data;
    private final Map<String, StatInstance> stats = new ConcurrentHashMap<>();

    /**
     * Update buffers address two issues.
     * <p>
     * First, it is useful to buffer stat updates on login because player
     * data has not been fully loaded yet. Since MythicLib does not have
     * long term storage for modifiers (especially stat modifiers related
     * to vanilla attributes-based stats, mostly Max Health), it needs to
     * wait for all plugins to load before sending any change to Bukkit.
     * <p>
     * It is also useful for MMOItems when refreshing a player's inventory and pause
     * updates until MMOItems has finished swooping the full player's inventory.
     * <p>
     * A player StatMap always starts in buffering mode.
     * On startup, player data is not loaded yet.
     *
     * @see StatMap#bufferUpdates()
     * @see StatMap#releaseUpdates()
     */
    private boolean bufferUpdates = true;

    public StatMap(MMOPlayerData player) {
        this.data = player;
    }

    /**
     * @return The StatMap owner ie the corresponding MMOPlayerData
     */
    @NotNull
    @Override
    public MMOPlayerData getData() {
        return data;
    }

    /**
     * @param stat The string key of the stat
     * @return The value of the stat after applying stat modifiers
     */
    @Override
    public double getStat(String stat) {
        return getInstance(stat).getFinal();
    }

    /**
     * StatInstances are completely flushed when the server restarts
     *
     * @param stat The string key of the stat
     * @return The corresponding StatInstance, which can be manipulated to add
     *         (temporary?) stat modifiers to a player, remove modifiers or
     *         calculate stat values in various ways.
     */
    @NotNull
    public StatInstance getInstance(String stat) {
        return stats.computeIfAbsent(stat, statId -> new StatInstance(this, statId));
    }

    /**
     * @return The StatInstances that have been manipulated so far since the
     *         player has logged in. StatInstances are completely flushed when
     *         the server restarts
     */
    @NotNull
    public Collection<StatInstance> getInstances() {
        return stats.values();
    }

    public void updateAll() {

        // Update caches and force updates
        for (StatHandler handler : MythicLib.plugin.getStats().getHandlers()) {
            final @Nullable StatInstance ins = handler.forcesUpdates() ? getInstance(handler.getStat()) : stats.get(handler.getStat());
            if (ins == null) continue;

            ins.flushCache(); // Sometimes handlers are cached before mmodatas are loaded
            ins.update(); // Update all stats, whatever
        }

        // Release updates
        releaseUpdates();
    }

    public void flushCache() {
        stats.values().forEach(StatInstance::flushCache);
    }

    public boolean isBufferingUpdates() {
        return bufferUpdates;
    }

    public void bufferUpdates(@NotNull Runnable runnable) {
        boolean buffered = !bufferUpdates;
        bufferUpdates();
        runnable.run();
        if (buffered) releaseUpdates();
    }

    /**
     * Not a safe method. Avoid using this method, and use the one
     * provided instead, as this method requires the use of {@link #releaseUpdates()}
     * which can mess with MMO plugin login logic.
     *
     * @see #bufferUpdates(Runnable)
     */
    public void bufferUpdates() {
        bufferUpdates = true;
    }

    public void releaseUpdates() {
        bufferUpdates = false;
        stats.values().forEach(StatInstance::releaseUpdates);
    }

    /**
     * @param castHand The casting hand matters a lot! Should MythicLib take into account
     *                 the 'Skill Damage' due to the offhand weapon, when casting a
     *                 skill with mainhand?
     * @return Some actions require the player stats to be temporarily saved.
     *         When a player casts a projectile skill, there's a brief delay
     *         before it hits the target: the stat values taken into account
     *         correspond to the stat values when the player cast the skill (not
     *         when it finally hits the target). This cache technique fixes a
     *         huge game breaking glitch
     */
    @NotNull
    @Override
    public PlayerMetadata cache(@NotNull EquipmentSlot castHand) {
        return new PlayerMetadata(this, castHand);
    }

    //region Deprecated

    @Deprecated
    public void update(String stat) {
        final StatInstance ins = stats.get(stat);
        if (ins != null) ins.update();
    }

    public MMOPlayerData getPlayerData() {
        return data;
    }

    //endregion
}
