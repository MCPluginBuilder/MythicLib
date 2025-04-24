package io.lumine.mythic.lib.api.stat.api;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.Closeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @see InstanceModifier
 */
public abstract class ModifiedInstance<T extends InstanceModifier> {
    protected final Map<UUID, T> modifiers = new ConcurrentHashMap<>();

    /**
     * @return The final modified value taking, into account the default value
     *         as well as all of the modifiers. %-based modifiers are applied
     *         afterwards, onto the sum of the base value + flat modifiers.
     */
    public double getTotal(double base) {
        return getFilteredTotal(base, EquipmentSlot.MAIN_HAND::isCompatible, mod -> mod);
    }

    /**
     * @param filter Filters modifiers taken into account for the final value computation
     * @return The final modified value taking, into account the default value
     *         as well as all of the modifiers. %-based modifiers are applied
     *         afterwards, onto the sum of the base value + flat modifiers.
     */
    public double getFilteredTotal(double base, Predicate<T> filter) {
        return getFilteredTotal(base, filter, mod -> mod);
    }

    /**
     * @param modification A modification to any modifier before taking it into account
     *                     in the final calculation.
     *                     This can be used for instance to reduce debuffs, by checking if
     *                     a stat modifier has a negative value and returning a modifier
     *                     with a reduced absolute value.
     * @return The final modified value taking, into account the default value
     *         as well as all of the modifiers. %-based modifiers are applied
     *         afterwards, onto the sum of the base value + flat modifiers.
     */
    public double getTotal(double base, Function<T, T> modification) {
        return getFilteredTotal(base, EquipmentSlot.MAIN_HAND::isCompatible, modification);
    }

    /**
     * @param filter       Filters modifiers taken into account for the final value computation
     * @param modification A modification to any modifier before taking it into account
     *                     in the final calculation.
     *                     This can be used for instance to reduce debuffs, by checking if
     *                     a stat modifier has a negative value and returning a modifier
     *                     with a reduced absolute value.
     * @return The final modified value taking, into account the default value
     *         as well as all of the modifiers. %-based modifiers are applied
     *         afterwards, onto the sum of the base value + flat modifiers.
     */
    public double getFilteredTotal(double base, Predicate<T> filter, Function<T, T> modification) {

        // Flat
        for (T mod : modifiers.values())
            if (mod.getType() == ModifierType.FLAT && filter.test(mod))
                base += modification.apply(mod).getValue();

        // Additive scalars
        double scalar = 1;
        for (T mod : modifiers.values())
            if (mod.getType() == ModifierType.ADDITIVE_MULTIPLIER && filter.test(mod))
                scalar += modification.apply(mod).getValue() / 100;
        base *= scalar;

        // Multiplicative scalars
        for (T mod : modifiers.values())
            if (mod.getType() == ModifierType.RELATIVE && filter.test(mod))
                base *= 1 + modification.apply(mod).getValue() / 100;

        return base;
    }

    @Nullable
    @Deprecated
    public T getModifier(@Nullable String key) {
        for (T modifier : modifiers.values())
            if (modifier.getKey().equals(key)) return modifier;
        return null;
    }

    /**
     * @param uniqueId The unique ID of the desired modifier
     * @return Modifier with given ID, or <code>null</code> if not found
     */
    @Nullable
    public T getModifier(@NotNull UUID uniqueId) {
        return modifiers.get(uniqueId);
    }

    @Deprecated
    public void addModifier(@NotNull T modifier) {
        removeIf(modifier.getKey()::equals);
        registerModifier(modifier);
    }

    public void registerModifier(@NotNull T modifier) {
        modifiers.put(modifier.getUniqueId(), modifier);
    }

    /**
     * Removes the modifier associated to the given unique ID.
     */
    public void removeModifier(@NotNull UUID uniqueId) {
        modifiers.remove(uniqueId);
    }

    /**
     * Removes the modifier associated to the given key.
     */
    @Deprecated
    public void remove(@NotNull String key) {
        removeIf(key::equals);
    }

    public boolean isEmpty() {
        return modifiers.isEmpty();
    }

    /**
     * Iterates through registered modifiers and unregisters them if a
     * certain condition based on their string key is met.
     *
     * @param condition Condition on the modifier key
     */
    public void removeIf(@NotNull Predicate<String> condition) {
        for (Iterator<T> iterator = modifiers.values().iterator(); iterator.hasNext(); ) {
            final T modifier = iterator.next();
            if (condition.test(modifier.getKey())) {
                if (modifier instanceof Closeable) ((Closeable) modifier).close();
                iterator.remove();
            }
        }
    }

    /**
     * @return All registered modifiers
     */
    @NotNull
    public Collection<T> getModifiers() {
        return modifiers.values();
    }

    @NotNull
    public Set<UUID> getIds() {
        return modifiers.keySet();
    }

    /**
     * @return All string keys of currently registered modifiers
     */
    @Deprecated
    @NotNull
    public Set<String> getKeys() {
        return modifiers.values().stream().map(PlayerModifier::getKey).collect(Collectors.toSet());
    }

    /**
     * @param key The string key of the external modifier source or plugin
     * @return If a modifier is registered with this key
     */
    public boolean contains(String key) {
        return modifiers.containsKey(key);
    }
}
