package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
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
import java.util.function.Function;

public abstract class TemporaryHandler implements Listener {

    /**
     * Handler lists which must be called when the temporary listener is closed
     * so that the listener is entirely unregistered
     */
    private final HandlerList[] handlerLists;

    private final JavaPlugin plugin;

    /**
     * When a player is attached to a temporary listener, this listener will close
     * if the player logs out or quits their current profile.
     */
    private final MMOPlayerData attachedPlayer;

    /**
     * Sometimes the close method is called twice because of a safe delayed task
     * not being cancelled when the listener is closed. It's set to true after
     * being closed at least once
     */
    private boolean open;

    /**
     * Temporary listeners often have a timed (or even delayed)
     * runnable scheduled on the side for skills or waiting times
     * for instance. This is purely optional.
     */
    @Nullable
    private BukkitRunnable runnable;

    public TemporaryHandler(@NotNull HandlerList... handlerLists) {
        this(MythicLib.plugin, handlerLists);
    }

    public TemporaryHandler(@NotNull MMOPlayerData attachedPlayer, @NotNull HandlerList... handlerLists) {
        this(MythicLib.plugin, attachedPlayer, handlerLists);
    }

    public TemporaryHandler(@NotNull JavaPlugin plugin, @NotNull HandlerList... handlerLists) {
        this(plugin, null, handlerLists);
    }

    /**
     * Util class to facilitate the temporary registration of event listeners
     * coupled to bukkit runnables.
     *
     * @param plugin         Plugin registering the listener
     * @param attachedPlayer Player to which this listener is attached
     * @param handlerLists   Handler lists to unregister when closed
     */
    public TemporaryHandler(@NotNull JavaPlugin plugin, @Nullable MMOPlayerData attachedPlayer, @NotNull HandlerList... handlerLists) {
        this.handlerLists = handlerLists.length == 0 ? inferHandlerLists(this.getClass()) : handlerLists;
        this.plugin = plugin;
        this.attachedPlayer = attachedPlayer;

        open();
    }

    public void open() {
        Validate.isTrue(!open, "Handler is already open");

        open = true;
        if (this.attachedPlayer != null) this.attachedPlayer.addTemporaryHandler(this);
        onOpen();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean close() {
        return closeNow(false);
    }

    /**
     * Immediately unregisters the listener.
     *
     * @param fromSession If called from the MMOPlayerData session closing.
     *                    If true, the listener won't try to remove itself from the session again
     * @return If it is the first time this method is called
     */
    public boolean closeNow(boolean fromSession) {
        if (!open) return false;

        open = false;
        if (!fromSession && this.attachedPlayer != null) this.attachedPlayer.removeTemporaryHandler(this);
        onClose();
        if (runnable != null && !runnable.isCancelled()) {
            runnable.cancel();
            runnable = null;
        }
        for (HandlerList list : handlerLists) list.unregister(this);
        return true;
    }

    /**
     * Unregisters the temporary listener after some delay
     *
     * @param duration Delay before un-registration
     */
    public void closeAfter(long duration) {
        Bukkit.getScheduler().runTaskLater(MythicLib.plugin, this::close, duration);
    }

    public void runTask(@NotNull Consumer<BukkitRunnable> action) {
        Validate.isTrue(this.runnable == null, "Runnable already registered");

        this.runnable = Objects.requireNonNull(newTask(), "#newTask() returned null");
        action.accept(runnable);
    }

    //region To be overridden

    @Nullable
    protected BukkitRunnable newTask() {
        // Must be overridden if used
        return null;
    }

    /**
     * Called when the listener is closed for the first time.
     * If {@link #close()} is called a second time after the listener
     * was already closed, this method will NOT get called a second time
     */
    protected void onClose() {
        // Nothing by default
    }

    protected void onOpen() {
        // Nothing by default
    }

    //endregion

    //region Static methods

    @NotNull
    public static TemporaryHandler timerTask(@NotNull MMOPlayerData attachedPlayer,
                                             long taskPeriod,
                                             @NotNull Function<TemporaryHandler, BukkitRunnable> task) {
        return task(attachedPlayer, runnable -> runnable.runTaskTimer(MythicLib.plugin, 0, taskPeriod), task);
    }

    @NotNull
    public static TemporaryHandler task(@NotNull MMOPlayerData attachedPlayer,
                                        @NotNull Consumer<BukkitRunnable> taskAction,
                                        @NotNull Function<TemporaryHandler, BukkitRunnable> task) {
        TemporaryHandler handler = new TemporaryHandler(attachedPlayer) {
            @Override
            protected BukkitRunnable newTask() {
                return task.apply(this);
            }
        };
        handler.runTask(taskAction);
        return handler;
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
            } catch (Exception any) {
                throw new RuntimeException("Could not infer events of temporary listener", any);
            }
        return lists.toArray(new HandlerList[0]);
    }

    private static boolean isEventClass(@NotNull Class<?> clazz) {
        final Class<?> superclass;
        return clazz == Event.class || ((superclass = clazz.getSuperclass()) != null && isEventClass(superclass));
    }

    //endregion
}
