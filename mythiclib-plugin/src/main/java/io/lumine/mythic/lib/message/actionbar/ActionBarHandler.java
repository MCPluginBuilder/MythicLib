package io.lumine.mythic.lib.message.actionbar;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.jetbrains.annotations.Nullable;

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
    private static final long DEFAULT_TIME_OUT = 30;

    public ActionBarHandler(MMOPlayerData playerData) {
        this.playerData = playerData;
    }

    public boolean canShow(int priority) {
        return !isBusy() || priority >= lastPriority;
    }

    public void hide(int priority, long duration) {
        show(priority + 1, duration, null);
    }

    public void show(@Nullable String message) {
        this.show(ActionBarPriority.NORMAL, DEFAULT_TIME_OUT, message);
    }

    public void show(int priority, @Nullable String message) {
        this.show(priority, DEFAULT_TIME_OUT, message);
    }

    /**
     * @param priority Message priority.
     * @param duration Actionbar message duration, or time out, in ticks
     * @param message  Formatted message. If null, action bar is hidden
     *                 for the provided duration instead of sending a message
     */
    public void show(int priority, long duration, @Nullable String message) {

        // Don't show message if busy and low priority.
        if (!canShow(priority)) return;

        // Update internal fields
        this.lastPriority = priority;
        this.timeOut = System.currentTimeMillis() + duration * 50;

        // Send message
        if (message != null) {
            var player = this.playerData.getPlayer();
            MythicLib.plugin.getVersion().getWrapper().sendActionBar(player, message);
        }
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
