package io.lumine.mythic.lib.script.mechanic.visual;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.LocationMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Location;
import org.bukkit.Sound;

@MechanicMetadata
public class SoundMechanic extends LocationMechanic {
    private final Sound bukkitSound;
    private final String soundString;
    private final DoubleFormula vol, pitch;

    public SoundMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("sound");

        soundString = config.getString("sound");

        Sound bukkitSound;
        try {
            bukkitSound = Sounds.fromName(UtilityMethods.enumName(soundString));
        } catch (Exception exception) {
            bukkitSound = null;
        }
        this.bukkitSound = bukkitSound;

        vol = config.getDoubleFormula("volume", DoubleFormula.constant(1));
        pitch = config.getDoubleFormula("pitch", DoubleFormula.constant(1));
    }

    @Override
    public void cast(SkillMetadata meta, Location loc) {
        final float vol = (float) this.vol.evaluate(meta);
        final float pitch = (float) this.pitch.evaluate(meta);

        if (bukkitSound == null) loc.getWorld().playSound(loc, soundString, vol, pitch);
        else loc.getWorld().playSound(loc, bukkitSound, vol, pitch);
    }
}