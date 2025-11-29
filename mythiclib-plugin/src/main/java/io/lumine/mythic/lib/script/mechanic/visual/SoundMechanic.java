package io.lumine.mythic.lib.script.mechanic.visual;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.LocationMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.util.ScriptSoundInformation;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;

@MechanicMetadata
public class SoundMechanic extends LocationMechanic {
    private final ScriptSoundInformation soundInfo;

    public SoundMechanic(ConfigObject config) {
        super(config);

        this.soundInfo = new ScriptSoundInformation(config);
    }

    @Override
    public void cast(SkillMetadata meta, Location loc) {
        final float vol = (float) soundInfo.vol.evaluate(meta);
        final float pitch = (float) soundInfo.pitch.evaluate(meta);

        if (soundInfo.bukkitSound == null) loc.getWorld().playSound(loc, soundInfo.assetId, vol, pitch);
        else loc.getWorld().playSound(loc, soundInfo.bukkitSound, vol, pitch);
    }
}