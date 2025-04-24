package io.lumine.mythic.lib.message;

import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundReader {
    private final String soundString;
    private final Sound soundEnum;
    private final float vol, pitch;

    public SoundReader(Object object) {

        // From string
        if (object instanceof String) {
            final String stringInput = (String) object;
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
            vol = (float) config.getDouble("volume", config.getDouble("vol"));
            pitch = (float) config.getDouble("pitch");
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
