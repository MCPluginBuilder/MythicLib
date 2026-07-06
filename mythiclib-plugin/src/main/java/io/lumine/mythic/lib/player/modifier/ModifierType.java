package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.util.Pair;
import org.jetbrains.annotations.NotNull;

public enum ModifierType {

    /**
     * Additive multiplier
     * <p>
     * Multiplies stat value by X%. Additive scalars stack up linearly,
     * which means +100% and +100% stack up to +200%. and not 210.
     */
    RELATIVE,

    /**
     * Compound multiplier
     * <p>
     * Multiplies final value by (1 + X / 100). Compound multipliers
     * "compound" with each other, they stack up geometrically.
     */
    COMPOUND,

    /**
     * Flat/Additive
     * <p>
     * Increases base stat value by a set/flat value.
     */
    FLAT;

    public String toStringSuffix() {
        switch (this) {
            case RELATIVE:
                return "%";
            case COMPOUND:
                return "s";
            case FLAT:
                return "";
            default:
                throw new IllegalStateException("Not implemented");
        }
    }

    public static Pair<ModifierType, Double> pairFromString(@NotNull String input) {
        ModifierType type = fromChar(input.toCharArray()[input.length() - 1]);
        double value = Double.parseDouble(type != ModifierType.FLAT ? input.substring(0, input.length() - 1) : input);
        return Pair.of(type, value);
    }

    private static ModifierType fromChar(char someChar) {
        switch (someChar) {
            case 'a':
            case '%':
                return RELATIVE;
            case 'c':
            case 'm':
                return COMPOUND;
            default:
                return FLAT;
        }
    }
}
