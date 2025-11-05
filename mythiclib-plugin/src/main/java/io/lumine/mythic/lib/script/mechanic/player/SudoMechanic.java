package io.lumine.mythic.lib.script.mechanic.player;

import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SudoMechanic extends TargetMechanic {
    private final String message;

    public SudoMechanic(ConfigObject config) {
        super(config);

        message = config.string("format", "fmt", "command", "cmd", "c", "f");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target entity must be a player");
        ((Player) target).performCommand(meta.parseString(message));
    }
}
