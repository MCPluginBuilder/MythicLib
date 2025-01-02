package io.lumine.mythic.lib.api.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class TemporaryListener implements Listener {

    /**
     * Handler lists which must be called when the temporary listener is closed
     * so that the listener is entirely unregistered
     */
    private final HandlerList[] handlerLists;

    /**
     * Sometimes the close method is called twice because of a safe delayed task
     * not being cancelled when the listener is closed. It's set to true after
     * being closed at least once
     */
    private boolean closed;

    /**
     * Temporary listeners often have a timed (or even delayed)
     * runnable scheduled on the side for skills or waiting times
     * for instance. This is purely optional.
     */
    @Nullable
    private BukkitRunnable runnable;

    public TemporaryListener(@NotNull HandlerList... handlerLists) {
        this(MythicLib.plugin, handlerLists);
    }

    /**
     * Used to register listeners which should be unregistered after a specific
     * period of time.
     *
     * @param plugin Plugin registering the listener
     */
    public TemporaryListener(@NotNull JavaPlugin plugin, @NotNull HandlerList... handlerLists) {
        this.handlerLists = handlerLists.length == 0 ? inferHandlerLists(this.getClass()) : handlerLists;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Unregisters the temporary listener after some delay
     *
     * @param duration Delay before unregistration
     */
    public void close(long duration) {
        Bukkit.getScheduler().runTaskLater(MythicLib.plugin, (Runnable) this::close, duration);
    }

    @NotNull
    public BukkitRunnable getRunnable() {
        return Objects.requireNonNull(runnable, "No runnable registered");
    }

    public void registerRunnable(@NotNull BukkitRunnable runnable, Consumer<BukkitRunnable> action) {
        Validate.notNull(runnable, "Runnable cannot be null");
        Validate.isTrue(this.runnable == null, "Runnable already registered");

        this.runnable = runnable;
        action.accept(runnable);
    }

    /**
     * Immediately unregisters the listener.
     *
     * @return If it is the first time this method is called
     */
    public boolean close() {
        if (closed) return false;

        closed = true;
        whenClosed();
        if (runnable != null && !runnable.isCancelled()) runnable.cancel();
        for (HandlerList list : handlerLists)
            list.unregister(this);
        return true;
    }

    /**
     * Called when the listener is closed for the first time.
     * If {@link #close()} is called a second time after the listener
     * was already closed, this method will NOT get called a second time
     */
    public void whenClosed() {
        // Nothing by default
    }

    public static HandlerList[] inferHandlerLists(@NotNull Class<?> clazz) {
        final List<HandlerList> lists = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods())
            try {
                EventHandler annot = method.getAnnotation(EventHandler.class);
                if (annot == null) continue;

                Validate.isTrue(method.getParameterCount() == 1, "Wrong param count for event handler");
                final Class<?> paramType = method.getParameters()[0].getType();
                Validate.isTrue(isEventClass(paramType), "Param of event handler is not an event class");
                final HandlerList handlerList = (HandlerList) paramType.getMethod("getHandlerList").invoke(null);
                lists.add(handlerList);
            } catch (Throwable any) {
                throw new RuntimeException("Could not infer events of temporary listener", any);
            }
        return lists.toArray(new HandlerList[0]);
    }

    private static boolean isEventClass(@NotNull Class<?> clazz) {
        final Class<?> superclass;
        return clazz == Event.class || ((superclass = clazz.getSuperclass()) != null && isEventClass(superclass));
    }
}
