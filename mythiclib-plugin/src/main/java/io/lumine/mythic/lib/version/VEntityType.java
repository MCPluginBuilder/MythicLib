package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.UtilityMethods;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public enum VEntityType {
    TNT("TNT", "PRIMED_TNT"),
    SNOW_GOLEM("SNOW_GOLEM", "SNOWMAN"),
    ;

    private final EntityType wrapped;

    VEntityType(String... candidates) {
        wrapped = UtilityMethods.resolveField(EntityType::valueOf, candidates);
    }

    @NotNull
    public EntityType get() {
        return wrapped;
    }
}