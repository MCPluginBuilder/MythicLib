package io.lumine.mythic.lib.comp.adventure.argument;

import org.jetbrains.annotations.NotNull;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 * <p>
 * This class contains information about the argument.
 */
public class AdventureArgument {

    private final String value;

    public AdventureArgument(@NotNull String value) {
        this.value = value;
    }

    /**
     * Returns the value of this argument, lower-cased in the root locale.
     *
     * <p>This value should be used for comparisons against literals, to help ensure MiniMessage tags are case-insensitive.</p>
     *
     * @return the lower-cased value of this argument
     */
    public @NotNull String toLowerCase() {
        return value.toLowerCase();
    }

    /**
     * Returns the value of this argument.
     *
     * @return the value
     */
    public @NotNull String value() {
        return value;
    }

    /**
     * Checks if this argument represents {@code true}.
     *
     * @return if this argument represents {@code true}
     */
    public boolean isTrue() {
        return "true".equals(this.value()) || "on".equals(this.value());
    }

    /**
     * Checks if this argument represents {@code false}.
     *
     * @return if this argument represents {@code false}
     */
    public boolean isFalse() {
        return "false".equals(this.value()) || "off".equals(this.value());
    }

    /**
     * Try and parse this argument as an {@code int}.
     *
     * <p>The optional will only be present if the value is a valid integer.</p>
     *
     * @return an optional providing the value of this argument as an integer
     * @since 4.10.0
     */
    public @NotNull OptionalInt asInt() {
        try {
            return OptionalInt.of(Integer.parseInt(this.value()));
        } catch (final NumberFormatException ex) {
            return OptionalInt.empty();
        }
    }

    /**
     * Try and parse this argument as a {@code double}.
     *
     * <p>The optional will only be present if the value is a valid double.</p>
     *
     * @return an optional providing the value of this argument as an integer
     * @since 4.10.0
     */
    public @NotNull OptionalDouble asDouble() {
        try {
            return OptionalDouble.of(Double.parseDouble(this.value()));
        } catch (final NumberFormatException ex) {
            return OptionalDouble.empty();
        }
    }
}
