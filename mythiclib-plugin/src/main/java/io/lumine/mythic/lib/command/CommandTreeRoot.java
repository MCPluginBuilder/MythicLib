package io.lumine.mythic.lib.command;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.command.argument.MissingArgumentException;
import io.lumine.mythic.lib.command.argument.PermissionException;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class CommandTreeRoot extends CommandTreeNode implements CommandExecutor, TabCompleter {
    private final String name, description, usageMessage;
    private final List<String> aliases;
    @Nullable
    private final String permission;
    private final VerboseMode verbose;

    public CommandTreeRoot(@NotNull ConfigurationSection config) {
        this(null, config);
    }

    public CommandTreeRoot(@Nullable BuiltinCommand command, @NotNull ConfigurationSection config) {
        super(null, command != null ? command.getLabel() : Objects.requireNonNull(config.getString("main"), "Command name cannot be null"));

        this.name = getId();
        this.description = config.getString("description", "");
        this.usageMessage = config.getString("usage", "/" + this.name);
        this.aliases = config.getStringList("aliases");
        this.permission = config.getString("permission");
        this.verbose = config.contains("verbose") ? UtilityMethods.prettyValueOf(VerboseMode::valueOf, config.getString("verbose"), "No verbose mode with ID '%s'") : VerboseMode.ALL;
    }

    @NotNull
    public VerboseMode getVerbose() {
        return verbose;
    }

    @Nullable
    public String getPermission() {
        return permission;
    }

    /**
     * @deprecated Not implemented
     */
    @Deprecated
    protected void setOnlyForPlayers() {
        // TODO
    }

    @NotNull
    public BukkitCommand toBukkit() {
        return new BukkitCommand(this.name, this.description, this.usageMessage, this.aliases) {

            @NotNull
            @Override
            public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
                if (permission != null && !sender.hasPermission(permission)) return new ArrayList<>();

                final var lastArg = args[args.length - 1].toLowerCase();
                final var candidates = new CommandTreeExplorer(sender, CommandTreeRoot.this, true, args).calculateTabCompletion();
                return candidates.stream().filter(string -> string.toLowerCase().startsWith(lastArg)).collect(Collectors.toList());
            }

            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
                if (permission != null && !sender.hasPermission(permission)) return false;

                final var explorer = new CommandTreeExplorer(sender, CommandTreeRoot.this, false, args);
                final var targetNode = explorer.getNode();
                final CommandResult executionResult;

                // Only properly catch COMMAND exceptions (arg, parsing, runtime..)
                try {
                    executionResult = targetNode.execute(explorer, sender, args);
                    Validate.notNull(executionResult, "Command execution result cannot be null");
                } catch (Exception exception) {
                    if (!(exception instanceof PermissionException))
                        explorer.fail(exception.getMessage());
                    if (exception instanceof MissingArgumentException) sendCommandUsage(explorer, targetNode);
                    //exception.printStackTrace();
                    return false;
                }

                // Show all existing commands
                if (executionResult == CommandResult.THROW_USAGE) sendCommandUsage(explorer, targetNode);

                // Command executed successfully
                return executionResult == CommandResult.SUCCESS;
            }
        };
    }

    private void sendCommandUsage(@NotNull CommandTreeExplorer explorer, @NotNull CommandTreeNode targetNode) {
        targetNode.calculateUsageList().forEach(str -> explorer.verbose(ChatColor.YELLOW + "/" + str));
    }

    //region Use as executor

    private final Lazy<BukkitCommand> bukkitCommandVm = Lazy.of(this::toBukkit);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return bukkitCommandVm.get().execute(sender, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return bukkitCommandVm.get().tabComplete(sender, label, args);
    }

    //endregion

    //region Deprecated

    @Deprecated
    public CommandTreeRoot(@NotNull String name) {
        this(name, (String) null);
    }

    @Deprecated
    public CommandTreeRoot(@NotNull String name, @Nullable String permission) {
        super(null, Objects.requireNonNull(name, "Command name cannot be null"));

        this.name = name;
        this.description = "";
        this.usageMessage = "/" + name;
        this.aliases = List.of();
        this.permission = permission;
        this.verbose = VerboseMode.ALL;
    }

    //endregion
}
