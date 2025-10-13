package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatsCommand extends CommandTreeNode {
    public StatsCommand(CommandTreeNode parent) {
        super(parent, "stats");
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You can only use this command as a player");
            return CommandResult.FAILURE;
        }

        final StatMap stats = MMOPlayerData.get((Player) sender).getStatMap();
        for (StatInstance ins : stats.getInstances()) {
            StringBuilder str = new StringBuilder(ins.getStat());
            str.append(" | Stat: ").append(ins.getFinal()).append(" | Base: ").append(ins.getBase()).append(" | ");
            for (StatModifier mod : ins.getModifiers())
                str.append(mod.toString()).append(" (").append(mod.getKey()).append(") + ");
            sender.sendMessage(str.toString());
        }

        return CommandResult.SUCCESS;
    }
}
