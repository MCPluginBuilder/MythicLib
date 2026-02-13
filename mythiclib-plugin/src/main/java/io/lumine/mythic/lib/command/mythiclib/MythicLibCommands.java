package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.command.BuiltinCommand;

import java.util.List;

public class MythicLibCommands {

    public static final BuiltinCommand MYTHICLIB = new BuiltinCommand(true, "mythiclib", MythicLibCommand::new);
    public static final BuiltinCommand SUPER_WORKBENCH = new BuiltinCommand("superworkbench", "mythiclib.superworkbench", "Open the super 5x5 workbench", SuperWorkbenchCommand::new, List.of("swb"));
    public static final BuiltinCommand MEGA_WORKBENCH = new BuiltinCommand("megaworkbench", "mythiclib.megaworkbench", "Open the mega 6x6 workbench", MegaWorkbenchCommand::new, List.of("mwb"));

    @Deprecated
    public static final BuiltinCommand HEALTH_SCALE = new BuiltinCommand("healthscale", "mythiclib.command.healthscale", "Deprecated, use '/ml debug healthscale' instead", HealthScaleCommand::new);
    @Deprecated
    public static final BuiltinCommand MMO_TEMP_STAT = new BuiltinCommand("mmotempstat", "mythiclib.tempstat", "Deprecated, use '/ml tempstat' instead", MMOTempStatCommand::new);
}
