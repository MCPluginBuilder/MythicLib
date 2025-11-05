package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.command.CommandTreeRoot;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.*;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.debug.DebugCommand;

public class MythicLibCommand extends CommandTreeRoot {

    @SuppressWarnings("deprecation")
    public MythicLibCommand() {
        super("mythiclib", "mythiclib.admin");

        addChild(new ReloadCommand(this));
        addChild(new CastCommand(this));
        addChild(new DamageCommand(this));
        addChild(new DebugCommand(this));
        addChild(new TempStatCommand(this));

        addChild(new StatModCommand(this)); // legacy
    }
}