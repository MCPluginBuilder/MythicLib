package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Util class to change how time delays are displayed
 */
public class DelayFormat {

    private final static char[] DELAY_CHARACTERS = {'s', 'm', 'h', 'd', 'M', 'y'};
    private final static long[] DELAY_AMOUNTS = {
            1000,
            1000 * 60,
            1000 * 60 * 60,
            1000 * 60 * 60 * 24,
            (long) (1000 * 60 * 60 * 24 * 30.436875),
            (long) (1000 * 60 * 60 * 24 * 365.2491)};
    private final static int N = DELAY_AMOUNTS.length;

    private final int[] chars;
    private final String[] translation;
    @Nullable
    private final String threshold, each;
    private final int smallestUnit;

    public DelayFormat() {
        chars = new int[N];
        for (int i = 0; i < N; i++) chars[i] = i;
        translation = null;
        threshold = null;
        each = null;
        smallestUnit = 0;
    }

    public DelayFormat(Object object) {

        // From a string. No translation, just a list of characters
        if (object instanceof String) {
            translation = null;
            threshold = null;
            each = null;
            chars = new int[object.toString().length()];
            smallestUnit = determineCharactersUsed(object.toString(), null);
        }

        // Anything is possible
        else if (object instanceof ConfigurationSection) {
            ConfigurationSection config = (ConfigurationSection) object;
            String format = Objects.requireNonNull(config.getString("format"), "Could not find format");
            chars = new int[format.length()];
            translation = new String[N];
            threshold = config.getString("threshold");
            each = config.getString("each");
            smallestUnit = determineCharactersUsed(format, config.getString("translate"));
        }

        // Error
        else throw new IllegalArgumentException("Provide either a config section or string");
    }

    private String[] loadTranslation(String input) {
        if (input == null) return null;

        if (input.contains(" ")) return input.split("\\ ");

        String[] stringArray = new String[input.length()];
        char[] charArray = input.toCharArray();
        for (int i = 0; i < charArray.length; i++)
            stringArray[i] = String.valueOf(charArray[i]);
        return stringArray;
    }

    private int determineCharactersUsed(@NotNull String chars, @Nullable String translation) {
        Validate.isTrue(!chars.isEmpty(), "Format cannot be empty");
        String[] translations = loadTranslation(translation);
        char[] tokens = chars.toCharArray();
        Validate.isTrue(translations == null || tokens.length == translations.length, "Format and translation don't have the same size");

        int smallest = N;
        for (int j = 0; j < chars.length(); j++) {
            char token = tokens[j];
            int index = indexOf(token);
            this.chars[j] = index;

            smallest = Math.min(smallest, index); // Find smallest unit

            // Save token translation
            if (translations != null) this.translation[j] = translations[j];
        }

        return smallest;
    }

    private int indexOf(char token) {
        for (int i = 0; i < N; i++)
            if (DelayFormat.DELAY_CHARACTERS[i] == token) return i;
        throw new IllegalArgumentException(String.format("Unknown token %s", token));
    }

    private String each(String stringToken, long value) {
        if (each == null) return value + stringToken + ' ';
        return String.format(each, value, stringToken);
    }

    public String format(long millis) {

        // Avoid displaying 0
        if (millis <= DELAY_AMOUNTS[smallestUnit])
            return threshold != null ? threshold : each(String.valueOf(DELAY_CHARACTERS[smallestUnit]), 1);

        // Offset to improve dynamic delay formatting
        millis += DELAY_AMOUNTS[smallestUnit] - 1;

        // Compute quotients
        long[] quotients = new long[N];
        for (int j = N - 1; j >= 0; j--) {
            long divisor = DELAY_AMOUNTS[j];
            quotients[j] = millis / divisor;
            millis = millis % divisor;
        }

        // Format
        StringBuilder builder = new StringBuilder();
        for (int index : chars) {
            long quotient = quotients[index];
            if (quotient == 0) continue; // Do not display 0's
            String stringToken = translation != null ? translation[index] : String.valueOf(DELAY_CHARACTERS[index]);
            builder.append(each(stringToken, quotient));
        }

        return builder.toString();
    }
}
