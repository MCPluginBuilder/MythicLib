package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.util.Pair;
import org.jetbrains.annotations.NotNull;

public enum ModifierType {

    /**
     * Compound multiplier
     * <p>
     * Multiplies final stat value by a set %.
     */
    RELATIVE,

    /**
     * Additive multiplier
     * <p>
     * Multiplies stat value by X%. Additive scalars stack up linearly,
     * which means +100% and +100% stack up to +200%.
     */
    ADDITIVE_MULTIPLIER,

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
            case ADDITIVE_MULTIPLIER:
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
            case '%':
            case 'c':
            case 'm':
                return RELATIVE;
            case 'a':
            case 's':
                return ADDITIVE_MULTIPLIER;
            default:
                return FLAT;
        }
    }
}
