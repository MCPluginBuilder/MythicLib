package io.lumine.mythic.lib.damage;

import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private DamageType[] types;
    private double value, additiveModifiers, multiplicativeModifiers = 1;

    @Nullable
    private Element element;

    public DamagePacket(double value, @NotNull DamageType... types) {
        this(value, null, types);
    }

    public DamagePacket(double value, @Nullable Element element, @NotNull DamageType... types) {
        this.value = value;
        this.types = types;
        this.element = element;
    }

    public double getValue() {
        return value;
    }

    /**
     * @return Damage types by which this damage packet scales
     */
    @NotNull
    public DamageType[] getTypes() {
        return types;
    }

    @Nullable
    public Element getElement() {
        return element;
    }

    public void setTypes(@NotNull DamageType[] types) {
        this.types = Objects.requireNonNull(types, "Damage type array cannot be null");
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
        Validate.isTrue(coefficient >= 0, "Coefficient cannot be negative");
        this.multiplicativeModifiers *= coefficient;
    }

    public void additiveModifier(double multiplier) {
        this.additiveModifiers += multiplier;
    }

    /**
     * @return Final value of the damage packet taking into account
     *         all the damage modifiers that have been registered
     */
    public double getFinalValue() {

        // Make sure the returned value is positive
        return value * Math.max(0, 1 + additiveModifiers) * multiplicativeModifiers;
    }

    /**
     * @return Checks if the current packet has that damage type
     */
    public boolean hasType(DamageType type) {

        for (DamageType checked : this.types)
            if (checked == type)
                return true;

        return false;
    }

    @Override
    public String toString() {
        StringBuilder damageTypes = new StringBuilder();

        // Append value and modifier
        damageTypes.append("\u00a7e").append("(").append(value)
                .append("*").append(additiveModifiers)
                .append("*").append(multiplicativeModifiers).append(")").append("x");

        // Append Scaling
        boolean damageAppended = false;
        for (DamageType type : types) {
            if (damageAppended) {
                damageTypes.append("\u00a73/");
            }
            damageAppended = true;

            // Color
            switch (type) {
                case WEAPON:
                    damageTypes.append("\u00a77");
                    break;
                case PHYSICAL:
                    damageTypes.append("\u00a78");
                    break;
                case PROJECTILE:
                    damageTypes.append("\u00a7a");
                    break;
                case MAGIC:
                    damageTypes.append("\u00a79");
                    break;
                case SKILL:
                    damageTypes.append("\u00a7f");
                    break;
                default:
                    damageTypes.append("\u00a7c");
                    break;
            }

            // Damage Type
            damageTypes.append(type);

            if (element != null)
                damageTypes.append(",El=").append(element.getId());
        }

        // Yeah
        return damageTypes.toString();
    }

    @Override
    public DamagePacket clone() {
        DamagePacket clone = new DamagePacket(value, types);
        clone.additiveModifiers = additiveModifiers;
        clone.multiplicativeModifiers = multiplicativeModifiers;
        clone.element = element;
        return clone;
    }
}