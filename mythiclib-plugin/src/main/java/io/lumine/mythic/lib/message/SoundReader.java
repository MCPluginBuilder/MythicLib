package io.lumine.mythic.lib.message;

import io.lumine.mythic.lib.util.config.YamlUtils;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundReader {

    @Nullable
    private final String soundString;

    @Nullable
    private final Sound soundEnum;

    private final float vol, pitch;

    public SoundReader(Object object) {

        // From string
        if (object instanceof String) {

            // From , separated string
            final String stringInput = (String) object;
            if (stringInput.contains(",")) {

                final var split = stringInput.split(",");
                final Sound tryParse = tryParseSoundEnum(split[0]);
                if (tryParse != null) {
                    soundEnum = tryParse;
                    soundString = null;
                } else {
                    soundString = split[0];
                    soundEnum = null;
                }

                var hasVol = split.length > 2;
                vol = hasVol ? Float.parseFloat(split[1]) : 1;
                pitch = Float.parseFloat(split[hasVol ? 2 : 1]);
                return;
            }

            final Sound tryParse = tryParseSoundEnum(stringInput);
            if (tryParse != null) {
                soundEnum = tryParse;
                soundString = null;
            } else {
                soundString = stringInput;
                soundEnum = null;
            }
            vol = 1;
            pitch = 1;
        }

        // From config section
        else if (object instanceof ConfigurationSection) {
            final ConfigurationSection config = (ConfigurationSection) object;
            final String stringInput = config.getString("sound");
            final @Nullable Sound tryParse = tryParseSoundEnum(stringInput);
            if (tryParse != null) {
                soundEnum = tryParse;
                soundString = null;
            } else {
                soundString = stringInput;
                soundEnum = null;
            }
            vol = YamlUtils.getFloat(config, "volume", "vol", "v");
            pitch = YamlUtils.getFloat(config, "pitch", "p");
        }

        // Error
        else throw new IllegalArgumentException("Expected either a string or config section");
    }

    @Nullable
    private Sound tryParseSoundEnum(String stringInput) {
        try {
            return Sounds.fromName(stringInput);
        } catch (Exception exception) {
            return null;
        }
    }

    public void play(@NotNull Player player) {
        if (soundEnum != null) player.playSound(player.getLocation(), soundEnum, vol, pitch);
        else player.playSound(player.getLocation(), soundString, vol, pitch);
    }
}
