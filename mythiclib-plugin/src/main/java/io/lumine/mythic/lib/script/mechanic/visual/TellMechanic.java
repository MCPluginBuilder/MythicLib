package io.lumine.mythic.lib.script.mechanic.visual;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@MechanicMetadata
public class TellMechanic extends TargetMechanic {
    private final String message;

    public TellMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("format");

        message = config.getString("format");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Can only send messages to players");

        // Apply placeholders
        String formatted = meta.parseString(message);

        // Send message
        target.sendMessage(formatted);
    }
}
