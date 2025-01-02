package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.api.event.fake.FakeBlockBreakEvent;
import io.lumine.mythic.lib.api.event.fake.FakeEntityDamageByEntityEvent;
import io.lumine.mythic.lib.api.event.fake.FakeEventCaller;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeEventManager {
    private final Map<Class<?>, List<FakeEventCaller>> callers = new HashMap<>();

    public FakeEventManager() {

        /*
         * This will add compatibility with many plugins with the need
         * for code-specific hooks. This classifies damage events with 0
         * damage as fake. These will be instantly ignored!
         */
        registerFakeEventCaller(EntityDamageEvent.class, damageEvent -> damageEvent.getDamage() == 0);

        // MythicLib fake events
        registerFakeEventCaller(EntityDamageEvent.class, damageEvent -> damageEvent instanceof FakeEntityDamageByEntityEvent);
        registerFakeEventCaller(BlockBreakEvent.class, event -> event instanceof FakeBlockBreakEvent);
    }

    public <E extends Event> void registerFakeEventCaller(Class<E> eventClass, @NotNull FakeEventCaller<E> caller) {
        Validate.notNull(eventClass, "Event class cannot be null");
        Validate.notNull(caller, "Fake event caller cannot be null");
        this.callers.computeIfAbsent(eventClass, unused -> new ArrayList<>()).add(caller);
    }

    public boolean isFake(@NotNull Event event) {
        Validate.notNull(event, "Event cannot be null");
        for (Map.Entry<Class<?>, List<FakeEventCaller>> entry : this.callers.entrySet())
            if (entry.getKey().isInstance(event))
                for (FakeEventCaller caller : entry.getValue()) if (caller.isFake(event)) return true;
        return false;
    }
}
