package io.lumine.mythic.lib.command.builtin.mythiclib;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeRoot;
import io.lumine.mythic.lib.command.builtin.mythiclib.debug.DebugCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class MythicLibCommand extends CommandTreeRoot {

    @SuppressWarnings("deprecation")
    public MythicLibCommand(@NotNull ConfigurationSection config) {
        super(config, MythicLib.plugin.getCommands().MYTHICLIB);

        addChild(new ReloadCommand(this));
        addChild(new CastCommand(this));
        addChild(new DamageCommand(this));
        addChild(new DebugCommand(this));
        addChild(new TempStatCommand(this));

        addChild(new StatModCommand(this)); // legacy
    }
}