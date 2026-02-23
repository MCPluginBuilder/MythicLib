package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeRoot;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class HealthScaleCommand extends CommandTreeRoot {
    public HealthScaleCommand(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        MythicLib.plugin.getLogger().info("/healthscale is deprecated. Use instead /ml debug healthscale");

        if (sender instanceof Player && sender.hasPermission("mythiclib.commands.healthscale")) {
            Player player = (Player) sender;
            player.setHealthScale(Double.parseDouble(args[0]));
            player.setHealthScaled(true);
            return CommandResult.SUCCESS;
        } else {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) return explorer.fail("That player does NOT exist!");

            player.setHealthScale(Double.parseDouble(args[1]));
            player.setHealthScaled(true);
            return explorer.success(args[0] + " Health has been scaled!");
        }
    }
}
