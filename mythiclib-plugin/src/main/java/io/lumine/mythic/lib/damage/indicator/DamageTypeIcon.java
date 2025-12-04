package io.lumine.mythic.lib.damage.indicator;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DamageTypeIcon {
    private final String crit, normal;

    public DamageTypeIcon(ConfigurationSection config) {
        this.normal = config.getString("normal");
        this.crit = config.getString("crit");
    }

    public DamageTypeIcon(@NotNull String normal, @Nullable String crit) {
        this.normal = normal;
        this.crit = crit;
    }

    @NotNull
    public String getIcon(boolean crit) {
        return crit && this.crit != null ? this.crit : this.normal;
    }

    @NotNull
    public static DamageTypeIcon fromConfig(Object configObject) {
        Validate.notNull(configObject, "DamageTypeIcon config cannot be null");

        if (configObject instanceof String) {
            return new DamageTypeIcon((String) configObject, null);
        }

        if (configObject instanceof ConfigurationSection) {
            return new DamageTypeIcon((ConfigurationSection) configObject);
        }

        throw new IllegalArgumentException("Invalid config object for DamageTypeIcon " + configObject.getClass().getName());
    }
}
