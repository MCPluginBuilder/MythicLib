package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.MMOCommandManager;
import io.lumine.mythic.lib.command.ToggleableCommand;
import io.lumine.mythic.lib.command.builtin.mythiclib.MythicLibCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class MythicLibCommandManager extends MMOCommandManager {
    public final ToggleableCommand MYTHICLIB = new ToggleableCommand("mythiclib", "mythiclib.admin", "Main admin command", MythicLibCommand::new, v -> true, true, "ml");

    @Override
    public JavaPlugin getPlugin() {
        return MythicLib.plugin;
    }

    @Override
    public List<ToggleableCommand> getAll() {
        return Arrays.asList(MYTHICLIB);
    }
}
