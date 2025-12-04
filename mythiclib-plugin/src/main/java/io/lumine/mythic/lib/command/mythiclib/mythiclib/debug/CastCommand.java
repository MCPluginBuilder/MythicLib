package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CastCommand extends CommandTreeNode {
    private final Argument<SkillHandler<?>> argSkill;

    public CastCommand(CommandTreeNode parent) {
        super(parent, "cast");

        argSkill = addArgument(Argument.SKILL_HANDLER);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return explorer.fail("This command is only for players");
        }

        SkillHandler<?> handler = explorer.parse(argSkill);
        new SimpleSkill(handler).cast(MMOPlayerData.get((Player) sender));
        return CommandResult.SUCCESS;
    }
}
