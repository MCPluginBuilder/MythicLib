package io.lumine.mythic.lib.api.event;

import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SynchronizedDataLoadEvent extends Event {
    private final SynchronizedDataManager<?, ?> manager;
    private final SynchronizedDataHolder holder;

    private static final HandlerList HANDLERS = new HandlerList();

    public SynchronizedDataLoadEvent(@NotNull SynchronizedDataManager<?, ?> manager, @NotNull SynchronizedDataHolder holder) {
        Validate.isTrue(!holder.getMMOPlayerData().isLookup(), "Cannot call event with lookup player data");

        this.holder = holder;
        this.manager = manager;
    }

    public SynchronizedDataManager<?, ?> getManager() {
        return manager;
    }

    public SynchronizedDataHolder getHolder() {
        return holder;
    }

    @Deprecated
    public boolean syncIsFull() {
        return holder.getMMOPlayerData().hasStartedPlaying();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
