package io.lumine.mythic.lib.api.stat.modifier;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.scheduler.BukkitRunnable;

public class TemporaryStatModifier extends StatModifier implements Closeable {
    private BukkitRunnable closeTask;
    private long duration, startTime;

    /**
     * Stat modifier given by an item, either a weapon or an armor piece.
     *
     * @param stat   Stat being modified
     * @param key    Player modifier key
     * @param value  Value of stat modifier
     * @param type   Is the modifier flat or multiplicative
     * @param slot   Slot of the item granting the stat modifier
     * @param source Type of the item granting the stat modifier
     */
    public TemporaryStatModifier(String key, String stat, double value, ModifierType type, EquipmentSlot slot, ModifierSource source) {
        super(key, stat, value, type, slot, source);
    }

    /**
     * @return Modifier duration in ticks
     */
    public long getDuration() {
        Validate.isTrue(isActive(), "Modifier is not active");
        return duration;
    }

    /**
     * @return Time stamp at which the modifier was registered
     */
    public long getStartTime() {
        Validate.isTrue(isActive(), "Modifier is not active");
        return startTime;
    }

    /**
     * Applies this modifier during a certain time
     *
     * @param playerData On whom is the modifier applied
     * @param duration   Time period after which the modifier will be unregistered
     */
    public void register(MMOPlayerData playerData, long duration) {
        Validate.isTrue(!isActive(), "Modifier is already active");
        super.register(playerData);
        closeTask = new BukkitRunnable() {
            @Override
            public void run() {
                unregister(playerData);
            }
        };
        closeTask.runTaskLater(MythicLib.plugin, duration);
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void register(MMOPlayerData playerData) {
        throw new UnsupportedOperationException("Use #register(MMOPlayerData, long) instead");
    }

    @Override
    public void close() {
        Validate.isTrue(isActive(), "Modifier is not active");
        closeTask.cancel();
        closeTask = null;
    }

    public boolean isActive() {
        return closeTask != null;
    }
}
