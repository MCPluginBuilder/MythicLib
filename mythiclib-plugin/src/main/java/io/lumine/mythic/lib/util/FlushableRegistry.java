package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * This is a registry that is automatically flushed at a specific frequency.
 * User has to provide the condition of removal and flush check in the constructor.
 * It can be loaded and will throw an error when trying to close it if it already is.
 * <p>
 * Most of the time, this acts as a security because objects should be removed from
 * the registry in an event-based fashion, this is why this class extends Listener.
 * <p>
 * Event registration is automatically handled by this class, both on class
 * instantiation and closure.
 *
 * @author jules
 */
public class FlushableRegistry<K, V> implements Closeable, Listener {
    private final Map<K, V> registry = new HashMap<>();
    private final BiPredicate<K, V> condition;
    private final long period;

    private final BukkitRunnable flushRunnable = new BukkitRunnable() {

        @Override
        public void run() {
            final Iterator<Map.Entry<K, V>> iterator = registry.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<K, V> next = iterator.next();
                if (condition.test(next.getKey(), next.getValue())) iterator.remove();
            }
        }
    };

    private boolean open = true;

    public FlushableRegistry(BiPredicate<K, V> condition, long period) {
        this.condition = condition;
        this.period = period;

        flushRunnable.runTaskTimer(MythicLib.plugin, period, period);
        Bukkit.getPluginManager().registerEvents(this, MythicLib.plugin);
    }

    @NotNull
    public Map<K, V> getRegistry() {
        return registry;
    }

    public boolean isOpen() {
        return open;
    }

    public long getPeriod() {
        return period;
    }

    @Override
    public void close() {
        Validate.isTrue(open, "Registry already closed");
        open = false;

        flushRunnable.cancel();
        registry.clear();
        HandlerList.unregisterAll(this);
    }
}
