package io.lumine.mythic.lib.script.mechanic.visual;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@MechanicMetadata
public class PlayerSoundMechanic extends TargetMechanic {
    private final Sound bukkitSound;
    private final String soundString;
    private final DoubleFormula vol, pitch;

    public PlayerSoundMechanic(ConfigObject config) {
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
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not player");

        final Player player = (Player) target;
        final float vol = (float) this.vol.evaluate(meta);
        final float pitch = (float) this.pitch.evaluate(meta);

        if (bukkitSound == null) player.playSound(target.getLocation(), soundString, vol, pitch);
        else player.playSound(target.getLocation(), bukkitSound, vol, pitch);
    }
}