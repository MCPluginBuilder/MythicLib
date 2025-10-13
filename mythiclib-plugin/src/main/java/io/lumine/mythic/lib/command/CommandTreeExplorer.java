package io.lumine.mythic.lib.command;

import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.command.argument.MissingArgumentException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class CommandTreeExplorer {
    private final String[] args;
    private final CommandSender sender;
    private final CommandTreeRoot root;

    private CommandTreeNode current;
    private int argCount;

    /**
     * Used to explore a command tree given a certain command
     *
     * @param sender The command sender
     * @param root   The command tree root to explore
     * @param args   Given arguments, tells what direction to take at every tree node
     */
    public CommandTreeExplorer(@NotNull CommandSender sender, @NotNull CommandTreeRoot root, @NotNull String[] args) {
        this.current = root;
        this.root = root;
        this.args = args;
        this.sender = sender;

        for (String arg : args)

            /*
             * Check if current command floor has the corresponding arg, if so
             * let the next floor handle the command.
             */
            if (argCount == 0 && current.hasChild(arg)) {
                current = current.getChild(arg);
            }

            /*
             * If the plugin cannot find a command tree node higher, then the
             * current floor node "handle" the command
             */
            else argCount++;
    }

    @NotNull
    public CommandTreeRoot getCommand() {
        return root;
    }

    @NotNull
    public CommandSender getSender() {
        return sender;
    }

    public <T> T parse(@NotNull Argument<T> arg) {
        return parse(arg, null);
    }

    public <T> T parse(@NotNull Argument<T> arg, @Nullable Function<CommandTreeExplorer, T> dynamicFallback) {
        final var argIndex = current.getLevel() + arg.getIndex();

        // Use fallback for default value if arg not present
        if (args.length <= argIndex) {
            final var fallback = dynamicFallback != null ? dynamicFallback : arg.getFallback();
            if (fallback == null) throw new MissingArgumentException(arg);
            return fallback.apply(this);
        }

        return arg.parse(this, args[argIndex]);
    }

    /**
     * @return The command tree node supported to handle the command
     */
    @NotNull
    public CommandTreeNode getNode() {
        return current;
    }

    @NotNull
    public String[] getArguments() {
        return args;
    }

    @NotNull
    public List<String> calculateTabCompletion() {
        return current.calculateTabCompletion(this, Math.max(0, argCount - 1));
    }
}