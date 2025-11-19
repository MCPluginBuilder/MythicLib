package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.command.CommandTreeRoot;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.*;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.debug.DebugCommand;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.stat.StatCommand;

public class MythicLibCommand extends CommandTreeRoot {

    @SuppressWarnings("deprecation")
    public MythicLibCommand() {
        super("mythiclib", "mythiclib.admin");

        addChild(new ReloadCommand(this));
        addChild(new CastCommand(this));
        addChild(new DamageCommand(this));
        addChild(new DebugCommand(this));
        addChild(new StatCommand(this));

        addChild(new TempStatCommand(this)); // legacy
        addChild(new StatModCommand(this)); // legacy
    }
}