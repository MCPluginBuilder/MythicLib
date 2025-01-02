package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.UtilityMethods;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum VEnchantment {
    POWER("power"),
    FORTUNE("fortune"),
    UNBREAKING("unbreaking"),
    ;

    private final Enchantment wrapped;

    VEnchantment(String... candidates) {
        wrapped = UtilityMethods.resolveField(VEnchantment::fromKey, candidates);
    }

    @Nullable
    private static Enchantment fromKey(@NotNull String key) {
        return Enchantment.getByKey(NamespacedKey.minecraft(key));
    }

    @NotNull
    public Enchantment get() {
        return wrapped;
    }
}
