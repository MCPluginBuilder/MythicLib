package io.lumine.mythic.lib.message.actionbar;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.jetbrains.annotations.NotNull;

/**
 * Used to centralize management of action bar messages in MythicLib
 *
 * @author Jules
 */
public class ActionBarHandler {

    private final MMOPlayerData playerData;

    private int lastPriority;
    private long lastUse;

    /**
     * Time delay before the action bar can be used
     * again for another message whatever the priority
     */
    private static final long TIME_OUT = 1500;

    public ActionBarHandler(MMOPlayerData playerData) {
        this.playerData = playerData;
    }

    public boolean canShow(int priority) {
        return isBusy() && priority < lastPriority;
    }

    public void show(@NotNull String message) {
        this.show(ActionBarPriority.NORMAL, message);
    }

    public void show(int priority, @NotNull String message) {

        // Don't show message if busy and low priority.
        if (canShow(priority)) return;

        // Update internal fields
        this.lastPriority = priority;
        this.lastUse = System.currentTimeMillis();

        // Send message
        var player = this.playerData.getPlayer();
        MythicLib.plugin.getVersion().getWrapper().sendActionBar(player, message);
    }

    public int getCurrentPriority() {
        return lastPriority;
    }

    public boolean isBusy() {
        return System.currentTimeMillis() < TIME_OUT + lastUse;
    }
}
