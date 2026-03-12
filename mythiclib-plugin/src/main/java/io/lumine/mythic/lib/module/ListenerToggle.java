package io.lumine.mythic.lib.module;

import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Thread-safe implementation of a toggleable listener.
 * Methods can only be called from the main server thread,
 * otherwise they will fail.
 * <p>
 * Listener class can extend Closeable to have its close()
 * method called when the listener is disabled.
 */
public class ListenerToggle {
    private final Supplier<Listener> listener;
    private final Plugin owner;

    // Internal state
    private Listener currentListener;

    public ListenerToggle(@NotNull Module module, Supplier<Listener> listener) {
        this(module.getPlugin(), listener);
    }

    public ListenerToggle(@NotNull Plugin owner, Supplier<Listener> listener) {
        this.listener = Lazy.of(listener);
        this.owner = owner;
    }

    public void toggle(boolean newState) {
        Validate.isTrue(Bukkit.isPrimaryThread(), "ListenerToggle can only be toggled from the main thread");

        // Enable
        if (newState && currentListener == null) {
            currentListener = this.listener.get();
            Bukkit.getPluginManager().registerEvents(currentListener, owner);
        }

        // Disable
        else if (!newState && currentListener != null) {

            var unregistered = this.currentListener;
            this.currentListener = null;
            HandlerList.unregisterAll(unregistered);

            if (unregistered instanceof Closeable) try {
                ((Closeable) unregistered).close();
            } catch (Exception exception) {
                throw new RuntimeException("Could not close listener", exception);
            }
        }
    }

    public void disable() {
        toggle(false);
    }
}
