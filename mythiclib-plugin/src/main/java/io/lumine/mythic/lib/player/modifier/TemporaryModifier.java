package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.util.annotation.NotUsed;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.scheduler.BukkitRunnable;

@Deprecated
@NotUsed
public class TemporaryModifier<T extends PlayerModifier> implements Closeable {
    private final T applied;
    private final int duration;

    private BukkitRunnable closeTask;
    private long startTime;

    @Deprecated
    @NotUsed
    public TemporaryModifier(T modifier, int duration) {
        this.applied = modifier;
        this.duration = duration;
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
     * @param playerData Player to apply modifier to
     */
    @Deprecated
    @NotUsed
    @SuppressWarnings("unchecked")
    public void register(MMOPlayerData playerData) {
        Validate.isTrue(!isActive(), "Modifier is already active");

        var parentMap = (ModifierMap<T>) this.applied.getMap(playerData); // Keep ref to modified stat map
        parentMap.addModifier(this.applied); // Apply modifier

        closeTask = new BukkitRunnable() {

            @Override
            public void run() {
                // Cannot call #unregister as this redirects to
                // the player's current stat map, which might have changed
                // in case of a profile switch or logout. Need to keep a
                // reference to the statMap
                parentMap.removeModifier(TemporaryModifier.this.applied.getUniqueId());
            }
        };
        closeTask.runTaskLater(MythicLib.plugin, this.duration);
        this.startTime = System.currentTimeMillis();
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
