package io.lumine.mythic.lib.api.stat.api;

import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Deprecated
public abstract class ModifiedInstance<T extends InstanceModifier> {

    @Deprecated
    public double getTotal(double base) {
        throw new IllegalStateException("Deprecated class");
    }

    @Deprecated
    public double getFilteredTotal(double base, Predicate<T> filter) {
        throw new IllegalStateException("Deprecated class");
    }

    @Deprecated
    public double getTotal(double base, Function<T, T> modification) {
        throw new IllegalStateException("Deprecated class");
    }

    @Deprecated
    public double getFilteredTotal(double base, Predicate<T> filter, Function<T, T> modification) {
        throw new IllegalStateException("Deprecated class");
    }

    @Deprecated
    public T getModifier(@Nullable String key) {
        if (key == null) return null;
        for (var mod : getModifiers())
            if (mod.getKey().equals(key))
                return mod;
        return null;
    }

    public T getModifier(@NotNull UUID uniqueId) {
        throw new IllegalStateException("Deprecated class");
    }

    @Deprecated
    public void addModifier(@NotNull T modifier) {
        removeIf(modifier.getKey()::equals);
        registerModifier(modifier);
    }

    public void registerModifier(@NotNull T modifier) {
        throw new IllegalStateException("Deprecated class");
    }

    public void removeModifier(@NotNull UUID uniqueId) {
        throw new IllegalStateException("Deprecated class");
    }

    @Deprecated
    public void remove(@NotNull String key) {
        removeIf(key::equals);
    }

    public boolean isEmpty() {
        throw new IllegalStateException("Deprecated class");
    }

    public void removeIf(@NotNull Predicate<String> condition) {
        throw new IllegalStateException("Deprecated class");
    }

    public Collection<T> getModifiers() {
        throw new IllegalStateException("Deprecated class");
    }

    public Set<UUID> getIds() {
        throw new IllegalStateException("Deprecated class");
    }

    @Deprecated
    public Set<String> getKeys() {
        return getModifiers().stream().map(PlayerModifier::getKey).collect(Collectors.toSet());
    }

    @Deprecated
    public boolean contains(@NotNull String key) {
        for (var mod : getModifiers())
            if (mod.getKey().equals(key))
                return true;
        return false;
    }
}
