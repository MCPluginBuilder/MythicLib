package io.lumine.mythic.lib.module;

import io.lumine.mythic.lib.util.Lazy;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ListenerToggle {

    /**
     * Using a lazy to avoid instantiating the listener to
     * avoid compatibility problems. Instanciator might call
     * methods or classes missing from the class loader.
     */
    private final Lazy<Listener> listener;

    private final Module module;

    private boolean currentState;

    public ListenerToggle(@NotNull Module module, Supplier<Listener> listener) {
        this.listener = Lazy.of(listener);
        this.module = module;
    }

    public synchronized void toggle(boolean newState) {

        // Enable
        if (newState && !currentState) {
            currentState = true;
            Bukkit.getPluginManager().registerEvents(this.listener.get(), this.module.getPlugin());
        }

        // Disable
        else if (!newState && currentState) {
            currentState = false;
            HandlerList.unregisterAll(this.listener.get());
        }
    }

    public synchronized void disable() {
        toggle(false);
    }
}
