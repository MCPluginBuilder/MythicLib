package io.lumine.mythic.lib.api.event.session;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.profile.ProfileSession;
import io.lumine.mythic.lib.profile.ProfileSessionState;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SessionUpdateEvent extends Event {
    private final MMOPlayerData player;
    private final ProfileSession session;
    private final ProfileSessionState oldState;
    private final ProfileSessionState newState;
    private final SessionUpdateReason reason;

    private static final HandlerList HANDLERS = new HandlerList();

    public SessionUpdateEvent(@NotNull MMOPlayerData player,
                              @NotNull ProfileSession session,
                              @NotNull SessionUpdateReason reason,
                              @NotNull ProfileSessionState oldState,
                              @NotNull ProfileSessionState newState) {
        this.player = player;
        this.session = session;
        this.reason = reason;
        this.oldState = oldState;
        this.newState = newState;
    }

    @NotNull
    public MMOPlayerData getPlayerData() {
        return player;
    }

    @NotNull
    public ProfileSession getSession() {
        return session;
    }

    @NotNull
    public SessionUpdateReason getReason() {
        return reason;
    }

    @NotNull
    public ProfileSessionState getOldState() {
        return oldState;
    }

    @NotNull
    public ProfileSessionState getNewState() {
        return newState;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
