package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.command.CommandTreeRoot;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.DamageCommand;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.ReloadCommand;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.StatModCommand;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.TempStatCommand;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.cooldown.CooldownCommand;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.debug.CastCommand;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.debug.DebugCommand;
import io.lumine.mythic.lib.command.mythiclib.mythiclib.stat.StatCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class MythicLibCommand extends CommandTreeRoot {

    @SuppressWarnings("deprecation")
    public MythicLibCommand(@NotNull ConfigurationSection config) {
        super(MythicLibCommands.MYTHICLIB, config);

        addChild(new ReloadCommand(this));
        addChild(new DamageCommand(this));
        addChild(new DebugCommand(this));
        addChild(new StatCommand(this));
        addChild(new CooldownCommand(this));

        addChild(new CastCommand(this)); // legacy
        addChild(new TempStatCommand(this)); // legacy
        addChild(new StatModCommand(this)); // legacy
    }
}