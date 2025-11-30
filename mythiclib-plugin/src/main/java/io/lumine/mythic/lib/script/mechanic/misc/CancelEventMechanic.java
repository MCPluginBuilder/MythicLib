package io.lumine.mythic.lib.script.mechanic.misc;

import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public class CancelEventMechanic extends Mechanic {
    public CancelEventMechanic(@NotNull ConfigObject ignore) {
        // Nothing
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        final var event = meta.getSourceEvent();
        Validate.isTrue(event instanceof Cancellable, "Source event is not cancellable");
        ((Cancellable) event).setCancelled(true);
    }
}
