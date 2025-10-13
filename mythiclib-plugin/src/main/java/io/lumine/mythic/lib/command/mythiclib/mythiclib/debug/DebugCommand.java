package io.lumine.mythic.lib.command.mythiclib.mythiclib.debug;

import io.lumine.mythic.lib.command.CommandTreeNode;

public class DebugCommand extends CommandTreeNode {

    @SuppressWarnings("deprecation")
    public DebugCommand(CommandTreeNode parent) {
        super(parent, "debug");

        addChild(new LogsCommand(this));
        // addChild(new NBTCommand(this));
        addChild(new StatsCommand(this));
        addChild(new VersionsCommand(this));
        addChild(new AttributesCommand(this));
        addChild(new HealthScaleCommand(this));
        addChild(new LegacyHealthScaleCommand(this));
        addChild(new TestCommand(this));
        addChild(new ParseCommand(this));
    }
}
