package io.lumine.mythic.lib.damage;

import io.lumine.mythic.lib.damage.indicator.DamageIndicator;
import io.lumine.mythic.lib.element.Element;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Contains all the information about damage
 * being dealt during a specific attack.
 *
 * @author Jules
 * @see AttackMetadata
 */
public class DamageMetadata implements Cloneable {
    private final List<DamagePacket> packets = new ArrayList<>();

    /**
     * The first damage packet to be registered inside of this damage
     * metadata. It is usually the most significant (highest value)
     * or at least the base damage on which all modifiers are then
     * applied.
     * <p>
     * This field is a direct reference of an existing element
     * of the collection returned by {@link #getPackets()}.
     */
    @NotNull
    private final DamagePacket initialPacket;

    // TODO change with custom damage types
    private final List<String> critTags = new ArrayList<>();

    public DamageMetadata(double damage, @NotNull List<DamageType> types) {
        this(damage, null, types);
    }

    public DamageMetadata(double damage, @Nullable Element element, @NotNull List<DamageType> types) {
        this(new DamagePacket(damage, element, types));
    }

    public DamageMetadata(@NotNull DamagePacket initialPacket) {
        this.initialPacket = Objects.requireNonNull(initialPacket, "Initial packet cannot be null");
        packets.add(initialPacket);
    }

    @NotNull
    public DamagePacket getInitialPacket() {
        return initialPacket;
    }

    /**
     * You cannot deal less than 0.01 damage. This is an arbitrary
     * positive constant, as MythicLib and other plugins consider
     * 0-damage events to be fake damage events used to check for
     * the PvP/PvE flag.
     */
    public static final double MINIMAL_DAMAGE = .01;

    public double getDamage() {
        double d = 0;

        for (DamagePacket packet : packets)
            d += packet.getFinalValue();

        return Math.max(MINIMAL_DAMAGE, d);
    }

    /**
     * @param element If null, non-elemental damage will be returned.
     */
    public double getDamage(@Nullable Element element) {
        double d = 0;

        for (DamagePacket packet : packets)
            if (Objects.equals(packet.getElement(), element)) d += packet.getFinalValue();

        return d;
    }

    public double getDamage(DamageType type) {
        double d = 0;

        for (DamagePacket packet : packets)
            if (packet.hasType(type)) d += packet.getFinalValue();

        return d;
    }

    @NotNull
    public List<DamagePacket> getPackets() {
        return packets;
    }

    /**
     * @return Set containing all the damage types found
     *         in all the different damage packets.
     */
    @NotNull
    public Set<DamageType> collectTypes() {
        final var collected = new HashSet<DamageType>();
        for (var packet : packets) collected.addAll(packet.getTypes());
        return collected;
    }

    /**
     * @return Iterates through all registered damage packets and
     *         see if any has one of the given damage types.
     */
    public boolean hasAnyType(@NotNull List<DamageType> damageTypes) {
        for (var candidate : damageTypes)
            for (var packet : packets) if (packet.hasType(candidate)) return true;
        return false;
    }

    /**
     * @return Iterates through all registered damage packets and
     *         see if any has this damage type.
     */
    public boolean hasType(DamageType type) {
        for (var packet : packets) if (packet.hasType(type)) return true;
        return false;
    }

    /**
     * @param element If null, will return true if it has non-elemental damage.
     * @return Iterates through all registered damage packets and
     *         see if any has this element.
     */
    public boolean hasElement(@Nullable Element element) {
        for (DamagePacket packet : packets)
            if (Objects.equals(packet.getElement(), element)) return true;

        return false;
    }

    //region Modifiers

    @NotNull
    public DamageMetadata add(double value, @NotNull DamageType... types) {
        return add(value, Arrays.asList(types));
    }

    /**
     * Registers a new damage packet.
     *
     * @param value Damage dealt by another source, this could be an on-hit
     *              skill increasing the damage of the current attack.
     * @param types The damage types of the packet being registered
     * @return The same modified damage metadata
     */
    @NotNull
    public DamageMetadata add(double value, @NotNull List<DamageType> types) {
        packets.add(new DamagePacket(value, types));
        return this;
    }

    @NotNull
    public DamageMetadata add(double value, @Nullable Element element, @NotNull DamageType... types) {
        return add(value, element, Arrays.asList(types));
    }

    /**
     * Registers a new elemental damage packet.
     *
     * @param value   Damage dealt by another source, this could be an on-hit
     *                skill increasing the damage of the current attack.
     * @param element The element
     * @param types   The damage types of the packet being registered
     * @return The same modified damage metadata
     */
    @NotNull
    public DamageMetadata add(double value, @Nullable Element element, @NotNull List<DamageType> types) {
        packets.add(new DamagePacket(value, element, types));
        return this;
    }

    /**
     * Register a multiplicative damage modifier in all damage packets.
     * <p>
     * This is used for critical strikes which modifier should
     * NOT stack up with damage boosting statistics.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata multiplicativeModifier(double coefficient) {
        for (DamagePacket packet : packets)
            packet.multiplicativeModifier(coefficient);
        return this;
    }

    /**
     * Registers a multiplicative damage modifier
     * which applies to any damage packet
     *
     * @param multiplier From 0 to infinity, 1 increases damage by 100%.
     *                   This can be negative as well
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata additiveModifier(double multiplier) {
        for (DamagePacket packet : packets)
            packet.additiveModifier(multiplier);
        return this;
    }

    /**
     * Register a multiplicative damage modifier for a specific damage type.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @param damageType  Specific damage type
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata multiplicativeModifier(double coefficient, @NotNull DamageType damageType) {
        for (var packet : packets) if (packet.hasType(damageType)) packet.multiplicativeModifier(coefficient);
        return this;
    }

    /**
     * Register a multiplicative damage modifier for a specific damage type.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @param damageType  Specific damage type
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata multiplicativeModifier(double coefficient, @NotNull List<DamageType> damageType) {
        for (var packet : packets) if (packet.hasAnyType(damageType)) packet.multiplicativeModifier(coefficient);
        return this;
    }

    /**
     * Register a multiplicative damage modifier for a specific element.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @param element     If null, non-elemental damage will be considered
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata multiplicativeModifier(double coefficient, @Nullable Element element) {
        for (var packet : packets)
            if (Objects.equals(packet.getElement(), element)) packet.multiplicativeModifier(coefficient);
        return this;
    }

    /**
     * Registers a multiplicative damage modifier which only
     * applies to a specific damage type
     *
     * @param multiplier From 0 to infinity, 1 increases damage by 100%.
     *                   This can be negative as well
     * @param damageType Specific damage type
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata additiveModifier(double multiplier, @NotNull DamageType damageType) {
        for (DamagePacket packet : packets)
            if (packet.hasType(damageType)) packet.additiveModifier(multiplier);
        return this;
    }

    /**
     * Register an additive damage modifier for a specific element.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     * @param element     If null, non-elemental damage will be considered
     * @return The same damage metadata
     */
    @NotNull
    public DamageMetadata additiveModifier(double coefficient, @NotNull Element element) {
        for (DamagePacket packet : packets)
            if (Objects.equals(packet.getElement(), element)) packet.additiveModifier(coefficient);
        return this;
    }

    //endregion

    @Override
    public DamageMetadata clone() {
        DamageMetadata clone = new DamageMetadata(initialPacket);
        for (DamagePacket packet : packets)
            clone.packets.add(packet.clone());
        return clone;
    }

    @Override
    public String toString() {

        StringBuilder damageTypes = new StringBuilder("\u00a73Damage Meta{");

        boolean packetAppended = false;
        for (DamagePacket packet : packets) {
            if (packetAppended) {
                damageTypes.append("\u00a73;");
            }
            packetAppended = true;

            // Damage
            damageTypes.append(packet);
        }

        // Yeah
        return damageTypes.append("\u00a73}").toString();
    }

    //region Deprecated

    @Deprecated
    public DamageMetadata(double damage, DamageType... types) {
        this(damage, null, Arrays.asList(types));
    }

    @Deprecated
    public DamageMetadata(double damage, @Nullable Element element, @NotNull DamageType... types) {
        this(damage, element, Arrays.asList(types));
    }

    @Deprecated
    public DamageMetadata() {
        this(0, List.of());
    }

    @NotNull
    @Deprecated
    public Set<Element> collectElements() {
        final Set<Element> collected = new HashSet<>();

        for (DamagePacket packet : packets)
            if (packet.getElement() != null) collected.add(packet.getElement());

        return collected;
    }

    @Deprecated
    @NotNull
    public Map<Element, Double> mapElementalDamage() {
        final Map<Element, Double> mapped = new HashMap<>();

        for (DamagePacket packet : packets)
            if (packet.getElement() != null)
                mapped.put(packet.getElement(), mapped.getOrDefault(packet.getElement(), 0d) + packet.getFinalValue());

        return mapped;
    }

    public boolean isCrit(DamageIndicator indicator) {
        // TODO rewrite after custom damage type update

        // element
        final var el = indicator.getElement();
        if (el != null && critTags.contains(el.getId())) return true;

        //dtypes
        for (var dtype : indicator.getTooltips())
            if (critTags.contains(dtype.name().toLowerCase())) return true;

        return false;
    }

    @Deprecated
    public boolean isWeaponCriticalStrike() {
        return critTags.contains("weapon");
    }

    @Deprecated
    public void registerWeaponCriticalStrike() {
        this.critTags.add("weapon");
    }

    @Deprecated
    public boolean isSkillCriticalStrike() {
        return critTags.contains("skill");
    }

    @Deprecated
    public void registerSkillCriticalStrike() {
        this.critTags.add("skill");
    }

    @Deprecated
    public void registerCrits(List<String> tags) {
        this.critTags.addAll(tags);
    }

    @Deprecated
    public boolean isElementalCriticalStrike(Element el) {
        return critTags.contains(el.getId());
    }

    @Deprecated
    public void registerElementalCriticalStrike(Element el) {
        this.critTags.add(el.getId());
    }

    //endregion
}
