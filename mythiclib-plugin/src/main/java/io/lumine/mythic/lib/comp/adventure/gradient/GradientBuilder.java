package io.lumine.mythic.lib.comp.adventure.gradient;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class GradientBuilder {

    public static String rgbGradient(String str, Color from, Color to, double phase, Interpolator interpolator) {
        return rgbGradient(str, from, to, phase, interpolator, new ArrayList<>());
    }

    /**
     * Create a gradient from a string from two colors with a phase.
     *
     * @param str          The string to parse.
     * @param from         The first color.
     * @param to           The second color.
     * @param phase        The phase.
     * @param interpolator The interpolator to use.
     * @param decorations  The decorations to apply.
     * @return The gradient.
     */
    public static String rgbGradient(String str, Color from, Color to, double phase, Interpolator interpolator, List<String> decorations) {
        final double[] red = interpolator.interpolate(from.getRed(), to.getRed(), str.length());
        final double[] green = interpolator.interpolate(from.getGreen(), to.getGreen(), str.length());
        final double[] blue = interpolator.interpolate(from.getBlue(), to.getBlue(), str.length());
        final StringBuilder builder = new StringBuilder();

        int start = str.length() - (int) (str.length() * phase);
        String decoration = String.join("", decorations);
        int charIndex = 0;
        for (int i = start; i < str.length(); i++) {
            builder.append(ChatColor.of(new Color(
                            (int) Math.round(red[i]),
                            (int) Math.round(green[i]),
                            (int) Math.round(blue[i]))))
                    .append(decoration)
                    .append(str.charAt(charIndex++));
        }
        for (int i = 0; i < start; i++) {
            builder.append(ChatColor.of(new Color(
                            (int) Math.round(red[i]),
                            (int) Math.round(green[i]),
                            (int) Math.round(blue[i]))))
                    .append(decoration)
                    .append(str.charAt(charIndex++));
        }
        return builder.toString();
    }

    /**
     * Create a gradient from a string from a colors array.
     *
     * @param str          The string to parse.
     * @param colors       The colors to use.
     * @param portions     The portions of each color.
     * @param interpolator The interpolator to use.
     * @return The gradient.
     */
    public static String multiRgbGradient(String str, Color[] colors, @Nullable double[] portions, Interpolator interpolator, List<String> decorations) {
        final double[] p;
        if (portions == null) {
            p = new double[colors.length - 1];
            Arrays.fill(p, 1 / (double) p.length);
        } else
            p = portions;

        Preconditions.checkArgument(colors.length >= 2);
        Preconditions.checkArgument(p.length == colors.length - 1);

        final StringBuilder builder = new StringBuilder();
        int stringIndex = 0;
        for (double portion : p) {
            final int length = (int) (portion * str.length());
            final String substring = str.substring(stringIndex, stringIndex + length);
            builder.append(rgbGradient(substring, colors[0], colors[1], 0d, interpolator, decorations));
            colors = Arrays.copyOfRange(colors, 1, colors.length);
            stringIndex += length;
        }

        if (stringIndex < str.length())
            builder.append(ChatColor.of(colors[colors.length - 1])).append(String.join("", decorations)).append(str.substring(stringIndex));
        return builder.toString();
    }

    /**
     * Create a gradient from a string from a colors array with a phase.
     *
     * @param str          The string to parse.
     * @param colors       The colors to use.
     * @param phase        The phase.
     * @param interpolator The interpolator to use.
     * @return The gradient.
     */
    public static String multiRgbGradient(String str, Color[] colors, double phase, Interpolator interpolator, List<String> decorations) {
        Color[] c = new Color[colors.length];
        for (int i = 0; i < colors.length; i++)
            c[i] = colors[(i + (int) (colors.length * phase)) % colors.length];
        return multiRgbGradient(str, c, null, interpolator, decorations);
    }
}
