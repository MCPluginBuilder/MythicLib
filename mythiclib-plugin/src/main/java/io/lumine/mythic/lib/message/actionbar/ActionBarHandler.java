package io.lumine.mythic.lib.message.actionbar;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Used to centralize management of action bar messages in MythicLib
 *
 * @author Jules
 */
public class ActionBarHandler {

    private final MMOPlayerData playerData;

    private int lastPriority;
    private long timeOut;

    /**
     * Time delay before the action bar can be used
     * again for another message whatever the priority.
     * <p>
     * Action bar messages take 1.5s to disappear
     * in vanilla Minecraft, equivalent to 30 ticks
     */
    public static final long DEFAULT_TIME_OUT = 30;

    public ActionBarHandler(MMOPlayerData playerData) {
        this.playerData = playerData;
    }

    public boolean canShow(int priority) {
        return !isBusy() || priority >= lastPriority;
    }

    public boolean hide(int priority, long duration) {
        return show(priority + 1, duration, (Supplier<String>) null);
    }

    public boolean show(@Nullable String message) {
        return this.show(ActionBarPriority.NORMAL, DEFAULT_TIME_OUT, message);
    }

    public boolean show(int priority, @Nullable String message) {
        return this.show(priority, DEFAULT_TIME_OUT, message);
    }

    public boolean show(int priority, long duration, @Nullable String message) {
        return show(priority, duration, message == null ? null : () -> message);
    }

    public boolean show(int priority, @Nullable Supplier<@NotNull String> message) {
        return this.show(priority, DEFAULT_TIME_OUT, message);
    }

    /**
     * @param priority Message priority.
     * @param duration Actionbar message duration, or time out, in ticks
     * @param message  Formatted message. If null, action bar is hidden
     *                 for the provided duration instead of sending a message
     */
    public boolean show(int priority, long duration, @Nullable Supplier<@NotNull String> message) {

        // Don't show message if busy and low priority.
        if (!canShow(priority)) return false;

        // Update internal fields
        this.lastPriority = priority;
        this.timeOut = System.currentTimeMillis() + duration * 50;

        // Send message
        if (message != null) {
            var player = this.playerData.getPlayer();
            var stringMessage = Objects.requireNonNull(message.get(), "Null message");
            MythicLib.plugin.getVersion().getWrapper().sendActionBar(player, stringMessage);
        }

        return true;
    }

    public void reset(int priority) {

        // Ignore if busy and low priority
        if (!canShow(priority)) return;

        this.lastPriority = priority;
        this.timeOut = 0;
    }

    public int getCurrentPriority() {
        return lastPriority;
    }

    public boolean isBusy() {
        return System.currentTimeMillis() < timeOut;
    }
}
