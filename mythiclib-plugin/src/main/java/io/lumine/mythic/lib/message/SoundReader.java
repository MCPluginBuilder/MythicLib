package io.lumine.mythic.lib.message;

import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundReader {

    @Nullable
    private final String soundString;

    @Nullable
    private final Sound soundEnum;

    private final float vol, pitch;

    @Deprecated
    public SoundReader(@NotNull ConfigurationSection config) {
        this(new ConfigSectionObject(config));
    }

    public SoundReader(@NotNull String stringInput) {
        // From , separated string
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

    public SoundReader(@NotNull ConfigObject config) {
        final String stringInput = config.string("sound", "snd", "s", "name", "n", "id");
        final @Nullable Sound tryParse = tryParseSoundEnum(stringInput);
        if (tryParse != null) {
            soundEnum = tryParse;
            soundString = null;
        } else {
            soundString = stringInput;
            soundEnum = null;
        }
        vol = config.flpt(1f, "volume", "vol", "v");
        pitch = config.flpt(1f, "pitch", "p");
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

    //region Static methods

    @Nullable
    @Contract("null -> null; !null -> !null")
    public static SoundReader fromConfig(@Nullable Object configObject) {

        if (configObject == null)
            return null;

        else if (configObject instanceof String)
            return new SoundReader((String) configObject);

        else if (configObject instanceof ConfigurationSection)
            return new SoundReader(new ConfigSectionObject((ConfigurationSection) configObject));

        else throw new IllegalArgumentException("Expected either a string or config section");
    }
}
