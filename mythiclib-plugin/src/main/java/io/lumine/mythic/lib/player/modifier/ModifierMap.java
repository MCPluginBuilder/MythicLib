package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.Closeable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public abstract class ModifierMap<T extends PlayerModifier> {
    protected final MMOPlayerData playerData;
    protected final Map<UUID, T> modifiers = new HashMap<>();

    public ModifierMap(MMOPlayerData playerData) {
        this.playerData = playerData;
    }

    @NotNull
    public MMOPlayerData getPlayerData() {
        return playerData;
    }

    @NotNull
    public Collection<T> getModifiers() {
        return modifiers.values();
    }

    @NotNull
    public List<T> isolateModifiers(@NotNull EquipmentSlot hand) {
        final ArrayList<T> isolated = new ArrayList<>();

        for (T modifier : getModifiers())
            if (hand.isCompatible(modifier))
                isolated.add(modifier);

        return isolated;
    }

    @Nullable
    public T addModifier(T modifier) {
        final @Nullable T removed = modifiers.put(modifier.getUniqueId(), modifier);
        if (removed instanceof Closeable) ((Closeable) removed).close();
        return removed;
    }

    @Nullable
    public T removeModifier(UUID uuid) {
        final @Nullable T removed = modifiers.remove(uuid);
        if (removed instanceof Closeable) ((Closeable) removed).close();
        return removed;
    }

    public void removeModifiersIf(Predicate<String> predicate) {
        final Iterator<T> iterator = modifiers.values().iterator();
        while (iterator.hasNext()) {
            final T skill = iterator.next();
            if (predicate.test(skill.getKey())) {
                iterator.remove();
                if (skill instanceof Closeable)
                    ((Closeable) skill).close();
            }
        }
    }

    public void removeModifiers(String key) {
        final Iterator<T> iterator = modifiers.values().iterator();
        while (iterator.hasNext()) {
            final T skill = iterator.next();
            if (skill.getKey().equals(key)) {
                iterator.remove();
                if (skill instanceof Closeable)
                    ((Closeable) skill).close();
            }
        }
    }
}
