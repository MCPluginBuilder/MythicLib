package io.lumine.mythic.lib.api.event;

import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

public class SynchronizedDataLoadEvent extends Event {
    private final SynchronizedDataManager<?, ?> manager;
    private final SynchronizedDataHolder holder;

    @Nullable
    private final Event profileEvent;

    private static final HandlerList HANDLERS = new HandlerList();

    public SynchronizedDataLoadEvent(SynchronizedDataManager<?, ?> manager, SynchronizedDataHolder holder) {
        this(manager, holder, null);
    }

    public SynchronizedDataLoadEvent(@NotNull SynchronizedDataManager<?, ?> manager, @NotNull SynchronizedDataHolder holder, @Nullable Event profileEvent) {
        this.holder = holder;
        this.manager = manager;
        this.profileEvent = profileEvent;
    }

    public SynchronizedDataManager<?, ?> getManager() {
        return manager;
    }

    public SynchronizedDataHolder getHolder() {
        return holder;
    }

    public boolean syncIsFull() {
        return holder.getMMOPlayerData().hasFullySynchronized();
    }

    @NotNull
    public Event getProfileEvent() {
        return Objects.requireNonNull(profileEvent, "No corresponding profile event");
    }

    public boolean hasProfileEvent() {
        return profileEvent != null;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
