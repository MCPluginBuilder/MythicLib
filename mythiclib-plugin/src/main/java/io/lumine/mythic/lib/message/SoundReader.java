package io.lumine.mythic.lib.message;

import io.lumine.mythic.lib.util.Pair;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundReader {

    @Nullable("if Bukkit sound")
    private final String assetName;

    @Nullable("if custom sound")
    private final Sound bukkitSound;

    private final float vol, pitch;

    @Deprecated
    public SoundReader(@NotNull ConfigurationSection config) {
        this(new ConfigSectionObject(config));
    }

    public SoundReader(@NotNull String stringInput) {
        // From , separated string
        if (stringInput.contains(",")) {

            final var split = stringInput.split(",");
            final var tryParse = parseSound(split[0]);
            bukkitSound = tryParse.getLeft();
            assetName = tryParse.getRight();

            var hasVol = split.length > 2;
            vol = hasVol ? Float.parseFloat(split[1]) : 1;
            pitch = Float.parseFloat(split[hasVol ? 2 : 1]);
            return;
        }

        final var parsedSound = parseSound(stringInput);
        bukkitSound = parsedSound.getLeft();
        assetName = parsedSound.getRight();
        vol = 1;
        pitch = 1;
    }

    public SoundReader(@NotNull ConfigObject config) {
        final String stringInput = config.string("sound", "snd", "s", "name", "n", "id");
        final var parsedSound = parseSound(stringInput);
        bukkitSound = parsedSound.getLeft();
        assetName = parsedSound.getRight();
        vol = config.flpt(1f, "volume", "vol", "v");
        pitch = config.flpt(1f, "pitch", "p");
    }

    public void play(@NotNull Player player) {
        if (bukkitSound != null) player.playSound(player.getLocation(), bukkitSound, vol, pitch);
        else player.playSound(player.getLocation(), assetName, vol, pitch);
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

    @NotNull
    private static Pair<Sound, String> parseSound(String stringInput) {

        // Try to parse as Bukkit sound
        try {
            return Pair.of(Sounds.fromName(stringInput), null);
        } catch (Exception ignored) {
            // Ignore
        }

        // Try to parse as a custom sound string
        try {
            Validate.notNull(NamespacedKey.fromString(stringInput), "Could not parse namespaced key");
            return Pair.of(null, stringInput);
        } catch (Exception ignored) {
        }

        throw new IllegalArgumentException("Invalid sound: '" + stringInput + "' is neither a Bukkit sound (UPPER_CASE_FORMAT) or Minecraft asset sound (namespace:key format)");
    }

    //endregion
}
