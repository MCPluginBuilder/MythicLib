package io.lumine.mythic.lib.api.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.provider.PlayerStatProvider;
import io.lumine.mythic.lib.player.PlayerDataMap;
import io.lumine.mythic.lib.player.PlayerMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StatMap extends PlayerDataMap implements PlayerStatProvider {
    private final MMOPlayerData data;
    private final Map<String, StatInstance> stats = new ConcurrentHashMap<>();

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

    @Override
    public @NotNull EquipmentSlot getActionHand() {
        return EquipmentSlot.MAIN_HAND;
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

    @Override
    public void onSessionOpen() {

        // Update caches and force updates
        for (var handler : MythicLib.plugin.getStats().getHandlers()) {
            final @Nullable var instance = handler.updateOnLogin() ? getInstance(handler.getStat()) : stats.get(handler.getStat());
            if (instance == null) continue;

            instance.invalidateReferences(); // Sometimes handlers are cached before player data are loaded
            instance.update(); // Update all stats, whatever
        }
    }

    @Override
    protected void onSessionClose() {
        invalidateReferences();
    }

    public void invalidateReferences() {
        stats.values().forEach(StatInstance::invalidateReferences);
    }

    //region Stat update buffers

    /**
     * It is useful to avoid multiple updates when applying multiple stat modifiers at once.
     * For instance, when a player equips multiple items at once, or when MMOCore applies
     * multiple buffs from varying sources.
     * <p>
     * This flag {@link #sessionOpen} also presents stat updates but for a different reason
     * (stat maps are disabled when profile session is not alive).
     * <p>
     * Multi-thread safe implementation of updates buffered using
     * an atomic integer to count the number of simultaneous threads
     * buffering updates.
     */
    private final AtomicInteger updatesBuffered = new AtomicInteger(0);

    public boolean isBufferingUpdates() {
        return updatesBuffered.get() > 0 || !sessionOpen;
    }

    public void bufferUpdates(@NotNull Runnable runnable) {
        updatesBuffered.incrementAndGet();
        try {
            runnable.run();
        } finally {
            var current = updatesBuffered.decrementAndGet();
            if (current == 0 && sessionOpen) stats.values().forEach(StatInstance::releaseUpdates);
        }
    }

    //endregion

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
