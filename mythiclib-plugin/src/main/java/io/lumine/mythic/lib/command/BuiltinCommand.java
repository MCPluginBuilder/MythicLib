package io.lumine.mythic.lib.command;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.config.YamlFile;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public class BuiltinCommand {
    private final boolean hardcoded;
    private final String label, configPath;
    private final String description, permission;
    private final Function<ConfigurationSection, CommandTreeRoot> builder;
    private final List<String> aliases;
    private final Supplier<Boolean> enabled;
    private final VerboseMode verbose;

    public BuiltinCommand(boolean hardcoded,
                          @NotNull String mainLabel,
                          @NotNull Function<ConfigurationSection, CommandTreeRoot> generator) {
        this(hardcoded, mainLabel, "ignore", "ignore", generator, null, null, List.of());
    }

    public BuiltinCommand(@NotNull String mainLabel,
                          @Nullable String permission,
                          @NotNull String description,
                          @NotNull Function<ConfigurationSection, CommandTreeRoot> generator) {
        this(false, mainLabel, permission, description, generator, null, null, List.of());
    }

    public BuiltinCommand(@NotNull String mainLabel,
                          @Nullable String permission,
                          @NotNull String description,
                          @NotNull Function<ConfigurationSection, CommandTreeRoot> generator,
                          @NotNull List<String> aliases) {
        this(false, mainLabel, permission, description, generator, null, null, aliases);
    }

    public BuiltinCommand(@NotNull String label,
                          @Nullable String permission,
                          @NotNull String description,
                          @NotNull Function<ConfigurationSection, CommandTreeRoot> builder,
                          @Nullable Supplier<Boolean> enabled,
                          @NotNull List<String> aliases) {
        this(false, label, permission, description, builder, enabled, null, aliases);
    }

    public BuiltinCommand(@NotNull String label,
                          @Nullable String permission,
                          @NotNull String description,
                          @NotNull Function<ConfigurationSection, CommandTreeRoot> builder,
                          @Nullable Supplier<Boolean> enabled,
                          @Nullable VerboseMode verbose,
                          @NotNull List<String> aliases) {
        this(false, label, permission, description, builder, enabled, verbose, aliases);
    }

    private BuiltinCommand(boolean hardcoded,
                           @NotNull String label,
                           @Nullable String permission,
                           @NotNull String description,
                           @NotNull Function<ConfigurationSection, CommandTreeRoot> builder,
                           @Nullable Supplier<Boolean> enabled,
                           @Nullable VerboseMode verbose,
                           @NotNull List<String> aliases) {
        this.hardcoded = hardcoded;
        this.label = label;
        this.configPath = label.toLowerCase().replace(" ", "-");
        this.permission = permission;
        this.description = description;
        this.builder = builder;
        this.aliases = aliases;
        this.verbose = Objects.requireNonNullElse(verbose, VerboseMode.ALL);
        this.enabled = enabled == null ? () -> true : enabled;
    }

    public boolean isHardcoded() {
        return hardcoded;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getPermission() {
        return permission;
    }

    @NotNull
    public List<String> getAliases() {
        return aliases;
    }

    @NotNull
    public String getConfigPath() {
        return configPath;
    }

    @NotNull
    public VerboseMode getVerbose() {
        return verbose;
    }

    @Nullable
    public CommandTreeRoot build(@NotNull ConfigurationSection config) {
        try {
            return builder.apply(config);
        } catch (CommandDisabledException exception) {
            // Command is disabled
            return null;
        }
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public static void initializeAll(@NotNull JavaPlugin plugin, @NotNull Class<?> enumClass) {
        final var list = new ArrayList<BuiltinCommand>();

        for (var field : enumClass.getDeclaredFields())
            if (field.getType().isAssignableFrom(BuiltinCommand.class)) try {
                list.add((BuiltinCommand) field.get(null));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        initializeAll(plugin, list);
    }

    public static <T extends BuiltinCommand> void initializeAll(@NotNull JavaPlugin plugin, @NotNull Iterable<T> values) {

        // Load default config file
        final var config = new YamlFile(plugin, "commands");
        if (!config.exists()) {
            for (var cmd : values) {
                final String path = cmd.getConfigPath();
                if (!cmd.isHardcoded()) {
                    config.getContent().set(path + ".main", cmd.getLabel());
                    config.getContent().set(path + ".aliases", cmd.getAliases());
                    config.getContent().set(path + ".description", cmd.getDescription());
                    config.getContent().set(path + ".permission", cmd.getPermission());
                }
                config.getContent().set(path + ".verbose", cmd.getVerbose().name());
            }

            config.save();
        }

        // Enable commands individually
        final var namespace = plugin.getName().toLowerCase();
        final var commandMap = UtilityMethods.getCommandMap();
        for (var cmd : values) {

            // If command is defined in plugin.yml, cannot be disabled
            if (cmd.isHardcoded()) {
                final var pluginCommand = plugin.getCommand(cmd.getLabel());
                Validate.notNull(pluginCommand, "Could not find hardcoded command " + cmd.getLabel() + " in plugin.yml");
                var section = config.getContent().getConfigurationSection(cmd.getConfigPath());
                if (section == null) {
                    plugin.getLogger().log(Level.WARNING, "Could not find config section for hardcoded command /" + cmd.getLabel() + " in commands.yml, make sure to include one!");
                    section = new YamlConfiguration();
                }
                final var commandRoot = cmd.build(section);
                if (commandRoot == null) continue;

                pluginCommand.setExecutor(commandRoot);
                pluginCommand.setTabCompleter(commandRoot);
            }

            // Toggleable command
            else if (cmd.isEnabled()) {
                Validate.isTrue(plugin.getCommand(cmd.getLabel()) == null, "Found hardcoded command " + cmd.getLabel() + " but it is not defined in plugin.yml");
                final var configObject = config.getContent().getConfigurationSection(cmd.getConfigPath());
                if (configObject == null) continue;

                var commandRoot = cmd.build(configObject);
                if (commandRoot == null) continue;

                commandMap.register(namespace, commandRoot.toBukkit());
            }
        }
    }
}
