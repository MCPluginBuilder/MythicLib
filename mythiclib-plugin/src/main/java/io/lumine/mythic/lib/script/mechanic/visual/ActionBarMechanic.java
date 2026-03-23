package io.lumine.mythic.lib.script.mechanic.visual;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@MechanicMetadata
public class ActionBarMechanic extends TargetMechanic {
    private final String message;
    private final NumericExpression duration, priority;

    public ActionBarMechanic(ConfigObject config) {
        super(config);

        message = config.string("message", "msg", "m", "format", "f");
        duration = config.numericExpr(NumericExpression.of(30), "duration", "dur", "d", "ticks", "time", "t");
        priority = config.numericExpr(NumericExpression.ZERO, "priority", "prior", "p", "level", "lvl", "l");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Can only send messages to players");

        // Apply placeholders
        final var formattedMessage = meta.parseString(message);
        final var duration = (int) this.duration.evaluate(meta);
        final var priority = (int) this.priority.evaluate(meta);

        Validate.isTrue(duration > 0, "Action bar message duration must be greater than 0 ticks");
        Validate.isTrue(priority >= 0, "Action bar message priority must be non-negative");

        // Send message
        MMOPlayerData.get(target.getUniqueId()).getActionBar().show(priority, duration, formattedMessage);
    }
}
