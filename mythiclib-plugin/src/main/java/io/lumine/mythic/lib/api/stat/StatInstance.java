package io.lumine.mythic.lib.api.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.api.ModifiedInstance;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author jules
 */
public class StatInstance extends ModifiedInstance<StatModifier> {
    @NotNull
    private final StatMap map;
    @NotNull
    private final String stat;

    /**
     * Can be empty at any time, since it can be flushed by events
     * like plugin reloads. Plugin reloads should flush all
     * existing references to StatHandlers as they potentially apply
     * modifications to max/min values, base values... of stats
     */
    @NotNull
    private final Lazy<Optional<StatHandler>> handler;

    public boolean updateRequired;

    public StatInstance(@NotNull StatMap map, @NotNull String stat) {
        this.map = map;
        this.stat = stat;
        this.handler = Lazy.persistent(() -> MythicLib.plugin.getStats().getHandler(stat));
    }

    @NotNull
    public StatMap getMap() {
        return map;
    }

    @NotNull
    public String getStat() {
        return stat;
    }

    public double getBase() {
        return handler.get().map(handler -> handler.getBaseValue(this)).orElse(0d);
    }

    /**
     * @return The final stat value taking into account the default stat value
     *         as well as the stat modifiers. The relative stat modifiers are
     *         applied afterward, onto the sum of the base value + flat modifiers.
     */
    public double getTotal() {
        return getFilteredTotal(EquipmentSlot.MAIN_HAND::isCompatible, mod -> mod);
    }

    public double getFinal() {
        return handler.get().map(handler -> handler.getFinalValue(this)).orElse(getTotal());
    }

    @NotNull
    public String formatFinal() {
        return format(getFinal());
    }

    @NotNull
    public String format(double value) {
        return handler.get().map(StatHandler::getDecimalFormat).orElse(MythicLib.plugin.getMMOConfig().decimal).format(value);
    }

    /**
     * @param filter Filters stat modifications taken into account for the calculation
     * @return The final stat value taking into account the default stat value
     *         as well as the stat modifiers. The relative stat modifiers are
     *         applied afterward, onto the sum of the base value + flat modifiers.
     */
    public double getFilteredTotal(Predicate<StatModifier> filter) {
        return getFilteredTotal(filter, mod -> mod);
    }

    /**
     * @param modification A modification to any stat modifier before taking it into
     *                     account in stat calculation. This can be used for instance to
     *                     reduce debuffs, by checking if a stat modifier has a negative
     *                     value and returning a modifier with a reduced absolute value
     * @return The final stat value taking into account the default stat value
     *         as well as the stat modifiers. The relative stat modifiers are
     *         applied afterwards, onto the sum of the base value + flat
     *         modifiers.
     */
    public double getTotal(Function<StatModifier, StatModifier> modification) {
        return getFilteredTotal(EquipmentSlot.MAIN_HAND::isCompatible, modification);
    }

    /**
     * @param filter       Filters stat modifications taken into account for the calculation
     * @param modification A modification to any stat modifier before taking it into
     *                     account in stat calculation. This can be used for instance to
     *                     reduce debuffs, by checking if a stat modifier has a negative
     *                     value and returning a modifier with a reduced absolute value
     * @return The final stat value taking into account the default stat value
     *         as well as the stat modifiers. The relative stat modifiers are
     *         applied afterward, onto the sum of the base value & flat modifiers.
     */
    public double getFilteredTotal(Predicate<StatModifier> filter, Function<StatModifier, StatModifier> modification) {
        final double total = getFilteredTotal(getBase(), filter, modification);
        return handler.get().map(statHandler -> statHandler.clampValue(total)).orElse(total);
    }

    /**
     * Registers a stat modifier and run the required player stat updates
     *
     * @param modifier The stat modifier being registered
     */
    @Override
    public void registerModifier(@NotNull StatModifier modifier) {
        final @Nullable StatModifier current = modifiers.put(modifier.getUniqueId(), modifier);
        if (modifier.equals(current)) return; // Safeguard, that should never happen tho

        if (current instanceof Closeable) ((Closeable) current).close();
        update();
    }

    /**
     * Iterates through registered stat modifiers and unregisters them if a
     * certain condition based on their string key is met
     *
     * @param condition Condition on the modifier key, if it should be
     *                  unregistered or not
     */
    @Override
    public void removeIf(@NotNull Predicate<String> condition) {
        boolean update = false;
        for (Iterator<StatModifier> iterator = modifiers.values().iterator(); iterator.hasNext(); ) {
            final StatModifier modifier = iterator.next();
            if (condition.test(modifier.getKey())) {
                if (modifier instanceof Closeable) ((Closeable) modifier).close();
                iterator.remove();
                update = true;
            }
        }

        if (update) update();
    }

    /**
     * Removes the modifier associated to the given unique ID.
     */
    @Override
    public void removeModifier(@NotNull UUID uniqueId) {

        // Find and remove current value
        final StatModifier mod = modifiers.remove(uniqueId);
        if (mod == null) return;

        /*
         * Closing modifier is really important with temporary stats because
         * otherwise the runnable will try to remove the key from the map even
         * though the attribute was cancelled beforehand
         */
        if (mod instanceof Closeable) ((Closeable) mod).close();

        update();
    }

    public void flushCache() {
        handler.flush();
    }

    //region Updating and Buffering

    /**
     * Forces an update on this stat instance. An important convention
     * is that NO UPDATES may be ran before all MMO plugins have loaded
     * their data. This gives time to other plugins to load in their
     * respective stat modifiers before updating vanilla stats like
     * Max Health, Movement Speed.
     */
    public void update() {
        if (map.isBufferingUpdates()) updateRequired = true;
        else handler.get().ifPresent(handler -> handler.runUpdate(this));
    }

    public void releaseUpdates() {
        Validate.isTrue(!map.isBufferingUpdates(), "StatMap is still in buffer mode");
        if (updateRequired) {
            handler.get().ifPresent(handler -> handler.runUpdate(this));
            updateRequired = false;
        }
    }

    //endregion

    //region Deprecated

    @Deprecated
    public ModifierPacket newPacket() {
        return new ModifierPacket();
    }

    @Deprecated
    public class ModifierPacket {
        private final boolean previousBuffer;

        @Deprecated
        public ModifierPacket() {
            previousBuffer = map.isBufferingUpdates();
            map.bufferUpdates();
        }

        @Deprecated
        public void addModifier(StatModifier modifier) {
            StatInstance.this.registerModifier(modifier);
        }

        @Deprecated
        public void remove(@NotNull UUID uniqueId) {
            StatInstance.this.removeModifier(uniqueId);
        }

        @Deprecated
        public void remove(@NotNull String key) {
            StatInstance.this.removeIf(str -> str.equals(key));
        }

        @Deprecated
        public void removeIf(@NotNull Predicate<String> condition) {
            StatInstance.this.removeIf(condition);
        }

        @Deprecated
        public void update() {
            if (!previousBuffer) StatInstance.this.releaseUpdates();
        }

        @Deprecated
        public void runUpdate() {
            update();
        }
    }

    @Override
    @Deprecated
    public void addModifier(@NotNull StatModifier modifier) {
        removeIf(modifier.getKey()::equals);
        registerModifier(modifier);
    }

    @Nullable
    @Deprecated
    public StatHandler findHandler() {
        return handler.get().orElse(null);
    }

    /**
     * Removes a stat modifier with a specific key
     *
     * @param key The string key of the external stat modifier source or plugin
     * @see #removeModifier(UUID)
     * @deprecated Modifiers are now uniquely identified by UUIDs
     */
    @Override
    @Deprecated
    public void remove(String key) {
        removeIf(key::equals);
    }

    //endregion
}

