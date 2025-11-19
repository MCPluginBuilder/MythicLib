package io.lumine.mythic.lib.command.mythiclib.mythiclib.stat;

import io.lumine.mythic.lib.command.CommandTreeNode;
import org.jetbrains.annotations.NotNull;

public class StatCommand extends CommandTreeNode {

    public StatCommand(@NotNull CommandTreeNode parent) {
        super(parent, "stat");

        addChild(new AddCommand(this));
        addChild(new CheckCommand(this));
        addChild(new RemoveCommand(this));
        addChild(new ClearCommand(this));
    }
}
