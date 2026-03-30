package io.lumine.mythic.lib.command;

import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.command.argument.ArgumentParseException;
import io.lumine.mythic.lib.command.argument.MissingArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
     * @param sender        The command sender
     * @param root          The command tree root to explore
     * @param tabCompletion Toggle on for tab completion
     * @param args          Given arguments, tells what direction to take at every tree node
     */
    public CommandTreeExplorer(@NotNull CommandSender sender, @NotNull CommandTreeRoot root, boolean tabCompletion, @NotNull String[] args) {
        this.current = root;
        this.root = root;
        this.args = args;
        this.sender = sender;

        for (var i = 0; i < args.length; i++) {
            final var arg = args[i];

            /*
             * Check if current command floor has the corresponding arg,
             * if so let the next floor handle the command.
             *
             * Small edge case - need to stop before the last one for tab completion
             */
            if (argCount == 0 && current.hasChild(arg) && (!tabCompletion || i < args.length - 1)) {
                current = current.getChild(arg);
            }

            /*
             * If the plugin cannot find a command tree node higher,
             * then the current tree level takes care of execution
             */
            else argCount++;
        }
    }

    @NotNull
    public CommandTreeRoot getCommand() {
        return root;
    }

    //region Verbose

    /**
     * Applies verbose rules relative to the current command as defined
     * in {@link VerboseMode}
     *
     * @param message Command feedback
     */
    @NotNull
    public CommandTreeNode.CommandResult fail(@Nullable String message) {
        if (message != null) verbose(ChatColor.RED + message);
        return CommandTreeNode.CommandResult.FAILURE;
    }

    /**
     * Applies verbose rules relative to the current command as defined
     * in {@link VerboseMode}
     *
     * @param message Command feedback
     */
    @NotNull
    public CommandTreeNode.CommandResult success(@Nullable String message) {
        if (message != null) verbose(ChatColor.YELLOW + message);
        return CommandTreeNode.CommandResult.SUCCESS;
    }

    public void verbose(@NotNull String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        switch (this.root.getVerbose()) {
            case ALL:
                sender.sendMessage(message);
                return;
            case PLAYER:
                if (sender instanceof Player) sender.sendMessage(message);
                return;
            case CONSOLE:
                if (!(sender instanceof Player)) sender.sendMessage(message);
                return;
            case REDIRECT_TO_CONSOLE:
                Bukkit.getConsoleSender().sendMessage(message);
                return;
            case NONE:
                // Pass
                return;
            default:
                throw new IllegalStateException("Unrecognized verbose mode " + root.getVerbose());
        }
    }

    //endregion

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
            try {
                return fallback.apply(this);
            } catch (Exception exception) {
                throw new ArgumentParseException("Fallback of argument " + arg.getKey() + " failed", exception);
            }
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