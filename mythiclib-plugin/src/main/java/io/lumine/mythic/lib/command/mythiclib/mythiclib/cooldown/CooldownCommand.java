package io.lumine.mythic.lib.command.mythiclib.mythiclib.cooldown;

import io.lumine.mythic.lib.command.CommandTreeNode;

public class CooldownCommand extends CommandTreeNode {
    public CooldownCommand(CommandTreeNode parent) {
        super(parent, "cooldown");

        addChild(new CheckCommand(this));
        addChild(new ResetCommand(this));
        addChild(new ApplyCommand(this));
        addChild(new ReduceCommand(this));
    }
}
