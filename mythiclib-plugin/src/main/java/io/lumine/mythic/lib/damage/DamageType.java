package io.lumine.mythic.lib.damage;

import io.lumine.mythic.lib.UtilityMethods;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum DamageType {

    /**
     * Magic damage dealt by magic weapons or abilities
     */
    MAGIC,

    /**
     * Physical damage dealt by melee attacks or skills
     */
    PHYSICAL,

    /**
     * Damage dealt by any type of weapon
     */
    WEAPON,

    /**
     * Damage dealt by skills or abilities
     */
    SKILL,

    /**
     * Projectile based weapons or skills
     */
    PROJECTILE,

    /**
     * Hitting an enemy with bare hands
     */
    UNARMED,

    /**
     * For use with {@link io.lumine.mythic.lib.comp.mythicmobs.mechanic.MMODamageMechanic}
     * and {@link io.lumine.mythic.lib.comp.mythicmobs.condition.HasDamageTypeCondition}
     * to make on-hit skills that inflict damage but don't infinitely loop themselves.
     */
    ON_HIT,

    /**
     * For use with {@link io.lumine.mythic.lib.comp.mythicmobs.mechanic.MMODamageMechanic}
     * and {@link io.lumine.mythic.lib.comp.mythicmobs.condition.HasDamageTypeCondition}
     * to make summoner class abilities, supposing you had a system for it built with MythicMobs
     * or another plugin (GooP Maybe!??);
     */
    MINION,

    /**
     * Damage over time
     */
    DOT;

    public String getPath() {
        return name().toLowerCase();
    }

    public String getOffenseStat() {
        return name() + "_DAMAGE";
    }

    @NotNull
    public static List<DamageType> listFromConfig(@NotNull List<DamageType> fallback, @Nullable Object object) {
        if (object == null) return Objects.requireNonNull(fallback, "Fallback damage types cannot be null");
        return listFromConfig(object);
    }

    @NotNull
    public static List<DamageType> listFromConfig(@NotNull Object object) {

        // From string
        if (object instanceof String) {

            var split = ((String) object).split(",");
            var list = new ArrayList<DamageType>(split.length);
            for (var s : split)
                list.add(UtilityMethods.prettyValueOf(DamageType::valueOf, s, "No damage type with name '%s'"));
            return list;
        }

        // From string list
        if (object instanceof List) {

            var list = new ArrayList<DamageType>(((List<?>) object).size());
            for (var o : (List<?>) object)
                list.add(UtilityMethods.prettyValueOf(DamageType::valueOf, o.toString(), "No damage type with name '%s'"));
            return list;
        }

        throw new IllegalArgumentException("Cannot parse DamageType list from " + object);
    }
}
