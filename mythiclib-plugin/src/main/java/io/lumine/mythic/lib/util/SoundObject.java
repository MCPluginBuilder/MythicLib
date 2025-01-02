package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

@Deprecated
public class SoundObject {

    @Nullable
    private final Sound sound;

    @Nullable
    private final String key;

    private final float volume;
    private final float pitch;

    @Deprecated
    public SoundObject(String input) {
        final String[] split = input.split(",");

        Sound sound = null;
        String key = null;
        try {
            sound = Sounds.fromName(UtilityMethods.enumName(split[0]));
        } catch (Exception ignored) {
            key = split[0];
        }

        this.sound = sound;
        this.key = key;

        volume = split.length > 1 ? Float.parseFloat(split[1]) : 1;
        pitch = split.length > 2 ? Float.parseFloat(split[2]) : 1;
    }

    @Deprecated
    public SoundObject(ConfigurationSection config) {
        final String input = config.getString("sound");

        Sound sound = null;
        String key = null;
        try {
            sound = Sounds.fromName(UtilityMethods.enumName(input));
        } catch (Exception ignored) {
            key = input;
        }

        this.sound = sound;
        this.key = key;

        volume = (float) config.getDouble("volume", 1);
        pitch = (float) config.getDouble("pitch", 1);
    }

    /**
     * @return If this object is custom a custom sound,
     *         potentially from a resource pack
     */
    public boolean isCustom() {
        return sound == null;
    }

    @Nullable
    public Sound getSound() {
        return sound;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void playTo(Player player) {
        playTo(player, volume, pitch);
    }

    public void playTo(Player player, float volume, float pitch) {
        if (isCustom())
            player.playSound(player.getLocation(), key, volume, pitch);
        else
            player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public void playAt(Location loc) {
        playAt(loc, volume, pitch);
    }

    public void playAt(Location loc, float volume, float pitch) {
        if (isCustom())
            loc.getWorld().playSound(loc, key, volume, pitch);
        else
            loc.getWorld().playSound(loc, sound, volume, pitch);
    }
}
