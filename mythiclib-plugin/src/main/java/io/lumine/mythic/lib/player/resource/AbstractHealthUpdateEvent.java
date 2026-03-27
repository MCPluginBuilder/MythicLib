package io.lumine.mythic.lib.player.resource;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public interface AbstractHealthUpdateEvent extends Cancellable {

    double getOldAmount();

    @NotNull
    ResourceUpdateReason getUpdateReason();

    double getNewAmount();
}
