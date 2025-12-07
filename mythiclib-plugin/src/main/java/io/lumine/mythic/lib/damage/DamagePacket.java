package io.lumine.mythic.lib.damage;

import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Some damage value weighted by a specific set of damage types. This helps
 * divide any attack into multiple parts that can be manipulated independently.
 * <p>
 * For instance, a melee sword attack would add one physical-weapon damage packet.
 * Then, casting an on-hit ability like Starfall would add an extra magic-skill
 * damage packet, independently of the packet that is already there. If we were
 * to then apply the 'Melee Damage' stat, it would only apply to the first packet.
 * <p>
 * Since 1.3.1 it is now possible to create implementations of the DamagePacket
 * class which can be used by other plugins to implement other mechanics.
 *
 * @author jules
 */
public class DamagePacket implements Cloneable {
    @NotNull
    private List<DamageType> types;
    private double value, additive, scalar = 1;

    @Nullable
    private Element element;

    public DamagePacket(double value, @NotNull List<DamageType> types) {
        this(value, null, types);
    }

    public DamagePacket(double value, @Nullable Element element, @NotNull List<DamageType> types) {
        this.value = value;
        this.types = types;
        this.element = element;
    }

    public double getValue() {
        return value;
    }

    /**
     * @return Damage types of the packet. Not guaranteed to be mutable.
     */
    @NotNull
    public List<DamageType> getTypes() {
        return types;
    }

    @Nullable
    public Element getElement() {
        return element;
    }

    public void setTypes(@Nullable List<DamageType> types) {
        this.types = Objects.requireNonNullElse(types, List.of());
    }

    /**
     * Directly edits the damage packet value.
     *
     * @param value New damage value
     */
    public void setValue(double value) {
        Validate.isTrue(value >= 0, "Value cannot be negative");
        this.value = value;
    }

    public void setElement(@Nullable Element element) {
        this.element = element;
    }

    /**
     * Register a multiplicative damage modifier.
     * <p>
     * This is used for critical strikes which modifier should
     * NOT stack up with damage boosting statistics.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     */
    public void multiplicativeModifier(double coefficient) {
        this.scalar *= coefficient;
    }

    public void additiveModifier(double multiplier) {
        this.additive += multiplier;
    }

    /**
     * @return Final value of the damage packet taking into account
     *         all the damage modifiers that have been registered
     */
    public double getFinalValue() {

        // Make sure the returned value is positive
        return value * Math.max(0, 1 + additive) * scalar;
    }

    /**
     * @return Checks if the current packet has that damage type
     */
    public boolean hasType(DamageType type) {
        for (var checked : this.types)
            if (checked == type) return true;
        return false;
    }

    /**
     * @return Checks if the current packet has any of the given damage types
     */
    public boolean hasAnyType(@NotNull List<DamageType> damageTypes) {
        for (var candidate : damageTypes) if (types.contains(candidate)) return true;
        return false;
    }

    @Override
    public String toString() {
        StringBuilder damageTypes = new StringBuilder();

        // Append value and modifier
        damageTypes.append("\u00a7e").append("(").append(value)
                .append("*").append(additive)
                .append("*").append(scalar).append(")").append("x");

        // Append Scaling
        boolean damageAppended = false;
        for (DamageType type : types) {
            if (damageAppended) {
                damageTypes.append("\u00a73/");
            }
            damageAppended = true;
            damageTypes.append(type);

            if (element != null)
                damageTypes.append(",El=").append(element.getId());
        }

        // Yeah
        return damageTypes.toString();
    }

    @Override
    public DamagePacket clone() {
        var clone = new DamagePacket(value, types);
        clone.additive = additive;
        clone.scalar = scalar;
        clone.element = element;
        return clone;
    }

    //region Deprecated

    @Deprecated
    public DamagePacket(double value, @NotNull DamageType... types) {
        this(value, null, Arrays.asList(types));
    }

    @Deprecated
    public DamagePacket(double value, @Nullable Element element, @NotNull DamageType... types) {
        this(value, element, Arrays.asList(types));
    }

    @Deprecated
    public void setTypes(DamageType[] types) {
        this.types = types == null ? List.of() : Arrays.asList(types);
    }

    //endregion
}