package io.lumine.mythic.lib.script.mechanic.player;

import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class KickMechanic extends TargetMechanic {
    private final String message;

    public KickMechanic(ConfigObject config) {
        super(config);

        message = config.getString("message", "You were kicked");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not a player");
        ((Player) target).kickPlayer(message);
    }
}
