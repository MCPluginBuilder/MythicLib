package io.lumine.mythic.lib.script.mechanic.misc;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.logging.Level;

@MechanicMetadata
public class DispatchCommandMechanic extends TargetMechanic {
    private final String format;
    private final boolean fromConsole, asOperator;

    public DispatchCommandMechanic(ConfigObject config) {
        super(config);

        format = config.string("format", "fmt", "f", "command", "cmd", "c");
        asOperator = config.bool(false, "op", "operator");
        fromConsole = config.bool(true, "from_console", "console", "s", "server", "from_server");
    }

    @Override
    public void cast(SkillMetadata meta, Entity targetEntity) {
        final var rawCommand = meta.parseString(format);

        // Can send with player instead?
        final var target = !fromConsole && targetEntity instanceof Player ? (Player) targetEntity : null;

        // Send as op
        if (target != null && asOperator && !target.isOp()) try {
            target.setOp(true);
            target.performCommand(rawCommand);
        } catch (Exception exception) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not run command '" + rawCommand + "' as entity '" + target.getUniqueId() + "': " + exception.getMessage());
        } finally {
            target.setOp(false);
        }

        // Just send
        if (target != null) target.performCommand(rawCommand);
        else Bukkit.dispatchCommand(Bukkit.getConsoleSender(), meta.parseString(format));
    }
}
