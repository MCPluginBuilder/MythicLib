package io.lumine.mythic.lib.command.api;

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
import java.util.stream.Collectors;

public abstract class CommandTreeRoot extends CommandTreeNode implements CommandExecutor, TabCompleter {
    protected final String permission;
    private final String description;
    private final List<String> aliases = new ArrayList<>();

    public CommandTreeRoot(@NotNull String id, @NotNull String permission) {
        super(null, id);

        this.permission = permission;
        this.description = "No description provided";
    }

    public CommandTreeRoot(@Nullable ConfigurationSection config, @NotNull ToggleableCommand command) {
        super(null, config == null ? command.getName() : config.getString("main"));

        this.permission = config == null ? command.getPermission() : config.getString("permission", command.getPermission());
        description = config == null ? command.getDescription() : config.getString("description", command.getDescription());
        if (config != null)
            this.aliases.addAll(config.getStringList("aliases"));
    }

    @NotNull
    public BukkitCommand getCommand() {
        return new BukkitCommand(getId(), description, "", aliases) {

            @NotNull
            @Override
            public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
                if (!sender.hasPermission(permission))
                    return new ArrayList<>();

                List<String> list = new CommandTreeExplorer(CommandTreeRoot.this, args).calculateTabCompletion();
                return args[args.length - 1].isEmpty() ? list
                        : list.stream().filter(string -> string.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
            }

            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
                if (!sender.hasPermission(permission))
                    return false;

                final CommandTreeNode explorer = new CommandTreeExplorer(CommandTreeRoot.this, args).getNode();
                final CommandResult executionResult = explorer.execute(sender, args);
                Validate.notNull(executionResult, "Command execution result cannot be null");
                if (executionResult == CommandResult.THROW_USAGE)
                    explorer.calculateUsageList().forEach(str -> sender.sendMessage(ChatColor.YELLOW + "/" + str));
                return executionResult == CommandResult.SUCCESS;
            }
        };
    }

    @Override
    @Deprecated
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }

    /**
     * This method is deprecated, to register a command you should use {@link MMOCommandManager} and register
     * a toggleable command in {@link MMOCommandManager#getAll()}.
     */
    @Deprecated
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(permission))
            return false;

        CommandTreeNode explorer = new CommandTreeExplorer(this, args).getNode();
        CommandResult res = explorer.execute(sender, args);
        Validate.notNull(res, "Command result cannot be null");
        if (res == CommandResult.THROW_USAGE)
            explorer.calculateUsageList().forEach(str -> sender.sendMessage(ChatColor.YELLOW + "/" + str));
        return res == CommandResult.SUCCESS;
    }

    /**
     * This method is deprecated, to register a command you should use {@link MMOCommandManager}
     * and register a toggleable command in {@link MMOCommandManager#getAll()}.
     */
    @Deprecated
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(permission))
            return new ArrayList<>();

        List<String> list = new CommandTreeExplorer(this, args).calculateTabCompletion();
        return args[args.length - 1].isEmpty() ? list
                : list.stream().filter(string -> string.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
    }
}
