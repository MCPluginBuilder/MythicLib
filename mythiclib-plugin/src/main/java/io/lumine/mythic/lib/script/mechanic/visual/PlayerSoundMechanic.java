package io.lumine.mythic.lib.script.mechanic.visual;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.util.ScriptSoundInformation;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@MechanicMetadata
public class PlayerSoundMechanic extends TargetMechanic {
    private final ScriptSoundInformation soundInfo;

    public PlayerSoundMechanic(ConfigObject config) {
        super(config);

        this.soundInfo = new ScriptSoundInformation(config);
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not player");

        final Player player = (Player) target;
        final float vol = (float) soundInfo.vol.evaluate(meta);
        final float pitch = (float) soundInfo.pitch.evaluate(meta);

        if (soundInfo.bukkitSound == null) player.playSound(target.getLocation(), soundInfo.assetId, vol, pitch);
        else player.playSound(target.getLocation(), soundInfo.bukkitSound, vol, pitch);
    }
}