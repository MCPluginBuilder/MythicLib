package io.lumine.mythic.lib.rpg.provided;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * @author Taken from MMOCore#ManaDisplayOptions
 */
public class ResourceDisplayOptions {
    private final String full, half, empty;
    private final char barCharacter;
    private final int length;

    public ResourceDisplayOptions(ConfigurationSection config) {
        Validate.notNull(config, "Could not load mana display options");

        full = MythicLib.plugin.parseColors(config.getString("color.full", "NO_INPUT"));
        half = MythicLib.plugin.parseColors(config.getString("color.half", "NO_INPUT"));
        empty = MythicLib.plugin.parseColors(config.getString("color.empty", "NO_INPUT"));

        var format = config.getString("char", "-");
        Validate.notEmpty(format, "Could not load mana bar character");
        barCharacter = format.charAt(0);

        length = config.getInt("length", 20);
        Validate.isTrue(length > 0, "Length must be positive");
    }

    @NotNull
    public String generateBar(double mana, double max) {
        StringBuilder format = new StringBuilder();
        double ratio = mana / max * this.length;

        for (int j = 1; j < this.length; j++)
            format.append(ratio >= j ? full : ratio >= j - .5 ? half : empty).append(barCharacter);

        return format.toString();
    }
}
