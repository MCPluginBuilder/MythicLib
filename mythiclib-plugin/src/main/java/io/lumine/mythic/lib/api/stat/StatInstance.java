package io.lumine.mythic.lib.api.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.api.ModifiedInstance;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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

    // TODO why is there a ConcurrentHashMap here?
    protected final Map<UUID, StatModifier> modifiers = new ConcurrentHashMap<>();

    /**
     * Can be empty at any time, since it can be flushed by events
     * like plugin reloads. Plugin reloads should flush all
     * existing references to StatHandlers as they potentially apply
     * modifications to max/min values, base values... of stats
     */
    @NotNull
    private final Lazy<Optional<StatHandler>> handler;

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

    public double getDefaultBase() {
        return handler.get().map(StatHandler::getPlayerDefaultBase).orElse(0d);
    }

    public double getFinal() {
        return getFinal(EquipmentSlot.MAIN_HAND);
    }

    /**
     * TOTAL stat value refers to the value after all MMO modifiers have been applied.
     * It differs from the FINAL stat value which can be further modified by the
     * Bukkit attribute system for vanilla stats like Max Health, Movement Speed...
     * <p>
     * For custom MythicLib stats, FINAL and TOTAL values are the same.
     * Most users should most likely interact with the FINAL stat value.
     *
     * @return The final stat value
     */
    public double getFinal(@NotNull EquipmentSlot hand) {
        return handler.get().map(handler -> {
            final var finalValue = handler.getFinalValue(this);
            if (hand == EquipmentSlot.MAIN_HAND) return finalValue;
            // Correct for offhand since player attributes are main-hand calculated.
            return finalValue - getTotal(getBase(), EquipmentSlot.MAIN_HAND) + getTotal(getBase(), EquipmentSlot.OFF_HAND);
        }).orElseGet(() -> getTotal(hand));
    }

    /**
     * TOTAL stat value refers to the value after all MMO modifiers have been applied.
     * It differs from the FINAL stat value which can be further modified by the
     * Bukkit attribute system for vanilla stats like Max Health, Movement Speed...
     * <p>
     * For custom MythicLib stats, FINAL and TOTAL values are the same.
     * Most users should most likely interact with the FINAL stat value.
     *
     * @return The formatted final stat value
     */
    @NotNull
    public String formatFinal() {
        return format(getFinal());
    }

    @NotNull
    public String format(double value) {
        return handler.get().map(StatHandler::getDecimalFormat).orElse(MythicLib.plugin.getMMOConfig().decimal).format(value);
    }

    /**
     * @param uniqueId The unique ID of the desired modifier
     * @return Modifier with given ID, or <code>null</code> if not found
     */
    @Nullable
    public StatModifier getModifier(@NotNull UUID uniqueId) {
        return modifiers.get(uniqueId);
    }

    /**
     * @return All registered modifiers
     */
    @NotNull
    public Collection<StatModifier> getModifiers() {
        return modifiers.values();
    }

    /**
     * @return All unique IDs of registered modifiers
     */
    @NotNull
    public Set<UUID> getIds() {
        return modifiers.keySet();
    }

    //region Stat Computation

    /**
     * TOTAL stat value refers to the value after all MMO modifiers have been applied.
     * It differs from the FINAL stat value which can be further modified by the
     * Bukkit attribute system for vanilla stats like Max Health, Movement Speed...
     * <p>
     * For custom MythicLib stats, FINAL and TOTAL values are the same.
     * Most users should most likely interact with the FINAL stat value.
     *
     * @return The total stat value taking into account the base & default stat values
     *         as well as the stat modifiers. Relative and scalar stat modifiers apply
     *         onto the sum of the base, default stat values and flat modifiers. The
     *         total stat value is computed with action hand set to MAIN_HAND.
     */
    public double getTotal() {
        return getTotal(getBase(), EquipmentSlot.MAIN_HAND);
    }

    /**
     * TOTAL stat value refers to the value after all MMO modifiers have been applied.
     * It differs from the FINAL stat value which can be further modified by the
     * Bukkit attribute system for vanilla stats like Max Health, Movement Speed...
     * <p>
     * For custom MythicLib stats, FINAL and TOTAL values are the same.
     * Most users should most likely interact with the FINAL stat value.
     *
     * @return The total stat value taking into account the provided base stat value
     *         as well as the stat modifiers. Relative and scalar stat modifiers apply
     *         onto the sum of the base, default stat values and flat modifiers.
     */
    public double getTotal(@NotNull EquipmentSlot actionHand) {
        return getTotal(getBase(), actionHand);
    }

    /**
     * TOTAL stat value refers to the value after all MMO modifiers have been applied.
     * It differs from the FINAL stat value which can be further modified by the
     * Bukkit attribute system for vanilla stats like Max Health, Movement Speed...
     * <p>
     * For custom MythicLib stats, FINAL and TOTAL values are the same.
     * Most users should most likely interact with the FINAL stat value.
     *
     * @return The total stat value taking into account the provided base stat value
     *         as well as the stat modifiers. Relative and scalar stat modifiers apply
     *         onto the sum of the base, default stat values and flat modifiers. The
     *         total stat value is computed with action hand set to MAIN_HAND.
     */
    public double getTotal(double base) {
        return getTotal(base, EquipmentSlot.MAIN_HAND);
    }

    /**
     * TOTAL stat value refers to the value after all MMO modifiers have been applied.
     * It differs from the FINAL stat value which can be further modified by the
     * Bukkit attribute system for vanilla stats like Max Health, Movement Speed...
     * <p>
     * For custom MythicLib stats, FINAL and TOTAL values are the same.
     * Most users should most likely interact with the FINAL stat value.
     *
     * @return The total stat value taking into account the provided base stat value
     *         as well as the stat modifiers. Relative and scalar stat modifiers apply
     *         onto the sum of the base, default stat values and flat modifiers.
     */
    public double getTotal(double base, @NotNull EquipmentSlot actionHand) {

        /////////////////////////
        // Check cache
        /////////////////////////
        var cachedValue = actionHand == EquipmentSlot.MAIN_HAND ? this.mainHandValueCache : offHandValueCache;
        if (cachedValue != null) return cachedValue;

        final var statHandler = this.handler.get();
        final @Nullable var transform = statHandler.map(StatHandler::getModifierEditor).orElse(null);

        // Allows for independent iterations for max parallelism
        var addScalar = 1d;
        var multScalar = 1d;

        for (var mod : modifiers.values()) {

            // Apply hand filter
            if (!actionHand.isCompatible(mod)) continue;

            // Apply modifier transformer
            mod = transform == null ? mod : transform.apply(this, mod);
            if (mod == null) continue;

            switch (mod.getType()) {

                case FLAT:
                    // Flat modifiers
                    base += mod.getValue();
                    continue;

                case RELATIVE:
                    // Additive scalars
                    addScalar += mod.getValue() / 100;
                    continue;

                case ADDITIVE_MULTIPLIER:
                    // Multiplicative/Compound scalars
                    // Bad naming
                    multScalar *= 1 + (mod.getValue() / 100);
            }
        }

        base = base * addScalar * multScalar;

        // Clamp stat value
        final var handler = this.handler.get();
        if (handler.isPresent()) base = handler.get().clampValue(base);

        return base;
    }

    //endregion

    /**
     * Registers a stat modifier and run the required player stat updates
     *
     * @param modifier The stat modifier being registered
     */
    public void registerModifier(@NotNull StatModifier modifier) {
        final @Nullable StatModifier current = modifiers.put(modifier.getUniqueId(), modifier);
        // TODO change "Closeable". add one interface Openable and have code run here instead
        // DO NOT TEST IF MODIFIER IS ALREADY IN THE MAP.
        if (current instanceof Closeable) ((Closeable) current).close();
        update();
    }

    /**
     * Iterates through registered stat modifiers and unregisters them if a
     * certain condition based on their string key is met
     *
     * @param keyCondition Condition on the modifier key, if it should be
     *                     unregistered or not
     */
    public void removeIf(@NotNull Predicate<String> keyCondition) {
        boolean update = false;
        for (var iterator = modifiers.values().iterator(); iterator.hasNext(); ) {
            final var modifier = iterator.next();
            if (keyCondition.test(modifier.getKey())) {
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

    /**
     * @return True if no modifier is registered on this stat instance
     */
    public boolean isEmpty() {
        return this.modifiers.isEmpty();
    }

    public void invalidateReferences() {
        handler.flush();
    }

    //region Updates, Buffering and Caches

    private final AtomicBoolean updateRequired = new AtomicBoolean(false);

    private @Nullable Double mainHandValueCache, offHandValueCache;

    /**
     * Forces an update on this stat instance. An important convention
     * is that NO UPDATES may be ran before all MMO plugins have loaded
     * their data. This gives time to other plugins to load in their
     * respective stat modifiers before updating vanilla stats like
     * Max Health, Movement Speed.
     */
    public void update() {

        // Invalid caches
        this.mainHandValueCache = null;
        this.offHandValueCache = null;

        if (map.isBufferingUpdates()) updateRequired.set(true);
        else handler.get().ifPresent(handler -> handler.runUpdates(this));
    }

    public void releaseUpdates() {
        if (updateRequired.getAndSet(false))
            handler.get().ifPresent(handler -> handler.runUpdates(this));
    }

    //endregion

    //region Deprecated

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

    @Deprecated
    public void remove(@NotNull String key) {
        removeIf(key::equals);
    }

    @Deprecated
    public double getFilteredTotal(double base,
                                   Predicate<StatModifier> filter,
                                   Function<StatModifier, StatModifier> modification) {

        // Allows for independent iterations for max parallelism
        var addScalar = 1d;
        var multScalar = 1d;

        for (var mod : modifiers.values())
            if (filter.test(mod)) switch (mod.getType()) {

                case FLAT:
                    // Flat modifiers
                    base += modification.apply(mod).getValue();
                    continue;

                case RELATIVE:
                    // Additive scalars
                    addScalar += modification.apply(mod).getValue() / 100;
                    continue;

                case ADDITIVE_MULTIPLIER:
                    // Multiplicative/Compound scalars
                    // Bad naming
                    multScalar *= 1 + (modification.apply(mod).getValue() / 100);
            }

        base = base * addScalar * multScalar;

        // Clamp stat value
        final var handler = this.handler.get();
        if (handler.isPresent()) base = handler.get().clampValue(base);

        return base;
    }

    @Deprecated
    public double getFilteredTotal(double base, @NotNull Predicate<StatModifier> filter) {
        return getFilteredTotal(base, filter, mod -> mod);
    }

    @Deprecated
    public double getTotal(double base, Function<StatModifier, StatModifier> modification) {
        return getFilteredTotal(base, EquipmentSlot.MAIN_HAND::isCompatible, modification);
    }

    @Deprecated
    public double getFilteredTotal(Predicate<StatModifier> filter) {
        return getFilteredTotal(filter, mod -> mod);
    }

    @Deprecated
    public double getTotal(Function<StatModifier, StatModifier> modification) {
        return getFilteredTotal(EquipmentSlot.MAIN_HAND::isCompatible, modification);
    }

    @Deprecated
    public double getFilteredTotal(Predicate<StatModifier> filter, Function<StatModifier, StatModifier> modification) {
        return getFilteredTotal(getBase(), filter, modification);
    }

    //endregion
}

