package io.lumine.mythic.lib.command.argument;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Argument<T> {
    private final String key;
    private final int indexInNode;
    private final BiConsumer<CommandTreeExplorer, List<String>> autoComplete;
    private final BiFunction<CommandTreeExplorer, String, T> parser;
    @Nullable
    private final Function<CommandTreeExplorer, T> fallback;

    private static final int INDEX_UNSET = -1;

    public Argument(@NotNull String key,
                    @NotNull BiConsumer<CommandTreeExplorer, List<String>> autoComplete,
                    @NotNull BiFunction<CommandTreeExplorer, String, T> parser) {
        this(key, INDEX_UNSET, autoComplete, parser, null);
    }

    public Argument(@NotNull String key,
                    @NotNull BiConsumer<CommandTreeExplorer, List<String>> autoComplete,
                    @NotNull BiFunction<CommandTreeExplorer, String, T> parser,
                    @Nullable Function<CommandTreeExplorer, T> fallback) {
        this(key, INDEX_UNSET, autoComplete, parser, fallback);
    }

    private Argument(@NotNull String key,
                     int indexInNode,
                     @NotNull BiConsumer<CommandTreeExplorer, List<String>> autoComplete,
                     @NotNull BiFunction<CommandTreeExplorer, String, T> parser,
                     @Nullable Function<CommandTreeExplorer, T> fallback) {
        this.key = key;
        this.indexInNode = indexInNode;
        this.autoComplete = autoComplete;
        this.parser = parser;
        this.fallback = fallback;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public boolean isOptional() {
        return fallback != null;
    }

    @Nullable
    public Function<CommandTreeExplorer, T> getFallback() {
        return fallback;
    }

    public T parse(@NotNull CommandTreeExplorer explorer, @NotNull String senderInput) {
        Validate.isTrue(parser != null, "No parser provided");

        return parser.apply(explorer, senderInput);
    }

    @NotNull
    public String format() {
        final var optional = isOptional();
        return (optional ? "(" : "<") + key + (optional ? ")" : ">");
    }

    public int getIndex() {
        Validate.isTrue(indexInNode != INDEX_UNSET, "Index not set");
        return indexInNode;
    }

    //region Adapting existing parameters

    @NotNull
    public Argument<T> withIndex(int indexInNode) {
        return new Argument<>(this.key, indexInNode, this.autoComplete, this.parser, this.fallback);
    }

    @NotNull
    public Argument<T> withAutoComplete(@NotNull BiConsumer<CommandTreeExplorer, List<String>> autoComplete) {
        return new Argument<>(this.key, this.indexInNode, autoComplete, this.parser, this.fallback);
    }

    @NotNull
    public Argument<T> required() {
        return new Argument<>(this.key, this.indexInNode, this.autoComplete, this.parser, null);
    }

    @NotNull
    public Argument<T> withKey(@NotNull String key) {
        return new Argument<>(key, this.indexInNode, this.autoComplete, this.parser, this.fallback);
    }

    @NotNull
    public Argument<T> withFallback(@Nullable Function<CommandTreeExplorer, T> fallback) {
        return new Argument<>(this.key, this.indexInNode, this.autoComplete, this.parser, fallback);
    }

    @NotNull
    private static <T> Function<CommandTreeExplorer, T> throwIfNoDynamicFallback() {
        return ignore -> {
            // Defer catching of error to runtime
            // Checking this statically would require tons of verbose
            throw new ArgumentParseException("No dynamic fallback provided");
        };
    }

    @NotNull
    public Argument<T> withDynamicFallback() {
        return new Argument<>(this.key, this.indexInNode, this.autoComplete, this.parser, throwIfNoDynamicFallback());
    }

    @NotNull
    public Argument<T> empty() {
        return new Argument<>(this.key, this.indexInNode, (explorer, list) -> {
        }, (explorer, input) -> null, explorer -> null);
    }

    //endregion

    public void autoComplete(@NotNull CommandTreeExplorer explorer, @NotNull List<String> list) {
        autoComplete.accept(explorer, list);
    }

    //region Ready to use parameters

    public static final Argument<@NotNull LivingEntity> LIVING_ENTITY = new Argument<>("entity",
            (explorer, list) -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())), (explorer, input) -> {

        // Also try by UUID
        try {
            final var asUniqueId = UUID.fromString(input); // If fails, fallback to name
            final var entity = Bukkit.getEntity(asUniqueId);
            Arguments.notNull(entity, "Could not find entity with UUID " + input);
            Arguments.isTrue(entity instanceof LivingEntity, "Entity is not living");
            return (LivingEntity) entity;
        } catch (Exception ignored) {
        }

        final var player = Bukkit.getPlayer(input);
        Arguments.notNull(player, "Could not find player " + input);
        return player;
    });

    public static final Argument<@NotNull Player> PLAYER = new Argument<>("player",
            (explorer, list) -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())),
            (explorer, input) -> {

                // Also try by UUID
                try {
                    final var asUniqueId = UUID.fromString(input); // If fails, fallback to name
                    final var player = Bukkit.getPlayer(asUniqueId);
                    Arguments.notNull(player, "Could not find player with UUID " + input);
                    return player;
                } catch (Exception ignored) {
                }

                final var player = Bukkit.getPlayer(input);
                Arguments.notNull(player, "Could not find player " + input);
                return player;
            });

    public static final Argument<@NotNull Player> PLAYER_OR_SENDER = new Argument<>("player",
            (explorer, list) -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())),
            (explorer, input) -> {
                final var player = Bukkit.getPlayer(input);
                Arguments.notNull(player, "Could not find player " + input);
                return player;
            }, explorer -> {
        if (explorer.getSender() instanceof Player) return (Player) explorer.getSender();
        throw new IllegalArgumentException("Please provide a player");
    });

    public static final Argument<@NotNull Integer> AMOUNT_INT = new Argument<>("amount", (explorer, list) -> {
        for (int j = 1; j <= 10; j++) list.add(String.valueOf(j));
    }, (explorer, input) -> {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            throw new ArgumentParseException(input + " is not a valid integer.", exception);
        }
    });

    public static final Argument<@NotNull SkillHandler<?>> SKILL_HANDLER = new Argument<>("skill_id", (explorer, list) -> {
        MythicLib.plugin.getSkills().getHandlers().forEach(handler -> list.add(handler.getId()));
    }, (explorer, input) -> {
        final SkillHandler<?> found = MythicLib.plugin.getSkills().getHandler(UtilityMethods.enumName(input));
        if (found == null) throw new ArgumentParseException("Could not find skill '" + input + "'");
        return found;
    });

    public static final Argument<@NotNull String> STRING = new Argument<>("string", (explorer, list) -> {
    }, (explorer, input) -> input);

    public static final Argument<@NotNull Double> AMOUNT_DOUBLE = new Argument<>("amount", (explorer, list) -> {
        for (int j = 1; j <= 10; j++) list.add(String.valueOf(j));
    }, (explorer, input) -> {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException exception) {
            throw new ArgumentParseException(input + " is not a valid number.", exception);
        }
    });

    public static final Argument<@NotNull Long> DURATION_TICKS = new Argument<>("duration", (explorer, list) -> {
        for (int j = 1; j <= 10; j += 2) list.add(String.valueOf(j * 20));
    }, (explorer, input) -> {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException exception) {
            throw new ArgumentParseException(input + " is not a valid integer", exception);
        }
    });

    public static final Argument<@NotNull Material> MATERIAL = new Argument<>("material", (explorer, list) -> {
        for (Material material : Material.values()) list.add(material.name());
    }, (explorer, input) -> {
        try {
            return Material.valueOf(UtilityMethods.enumName(input));
        } catch (IllegalArgumentException exception) {
            throw new ArgumentParseException(input + " is not a valid material.", exception);
        }
    });

    public static final Argument<@NotNull String> STAT = new Argument<>("stat", (explorer, list) -> {
        list.addAll(MythicLib.plugin.getStats().getRegisteredStats());
    }, (explorer, input) -> {
        //Validate.isTrue(MythicLib.plugin.getStats().isStatRegistered(stat), stat + " is not a valid stat.");
        // No validation!
        return UtilityMethods.enumName(input);
    });

    public static final Argument<@NotNull Boolean> BOOLEAN = new Argument<>("boolean", (explorer, list) -> {
        list.add("true");
        list.add("false");
    }, (explorer, input) -> {
        if (input.equalsIgnoreCase("true")) return true;
        if (input.equalsIgnoreCase("false")) return false;
        throw new ArgumentParseException(input + " is not a valid boolean.");
    });

    /**
     * @deprecated Not tested
     */
    @Deprecated
    public static <T extends Enum<T>> Argument<T> choices(@NotNull String key, @NotNull Class<T> enumClass) {
        final var asList = Arrays.asList(enumClass.getEnumConstants());
        return new Argument<>(key, (explorer, list) -> asList.forEach(a -> list.add(a.toString())), (explorer, input) -> {
            try {
                return Enum.valueOf(enumClass, UtilityMethods.enumName(input));
            } catch (IllegalArgumentException exception) {
                throw new ArgumentParseException(input + " is not a valid choice.", exception);
            }
        });
    }

    public static Argument<String> choices(@NotNull String key, @NotNull String... candidates) {
        final var asList = Arrays.asList(candidates);
        return new Argument<>(key, (explorer, list) -> list.addAll(asList), (explorer, input) -> {
            if (asList.contains(input)) return input;
            throw new ArgumentParseException(input + " is not a valid choice.");
        });
    }

    public static final Argument<@NotNull Attribute> VANILLA_ATTRIBUTE = new Argument<>("<attribute>",
            (explorer, list) -> Attributes.getAll().forEach(attribute -> list.add(Attributes.name(attribute))),
            (explorer, input) -> {
                try {
                    return Attributes.fromName(input);
                } catch (IllegalArgumentException exception) {
                    throw new ArgumentParseException(input + " is not a valid attribute.", exception);
                }
            });

    public static final String DEFAULT_MODIFIER_KEY = "default";

    /**
     * Key used to identify a player modifier
     */
    public static final Argument<String> MODIFIER_KEY = new Argument<>("key", (tree, list) -> {
        list.add(DEFAULT_MODIFIER_KEY);
        list.add("plugin_name");
        list.add("passive_skill_name");
    }, (explorer, input) -> input, explorer -> DEFAULT_MODIFIER_KEY);

    public static Argument<String> modifierKeyOf(Argument<@NotNull Player> argPlayer) {
        return new Argument<>("key", (tree, list) -> {

            // Collect at most once
            var collected = new HashSet<String>();
            var mmoPlayerData = MMOPlayerData.get(tree.parse(argPlayer));
            for (var inst : mmoPlayerData.getStatMap().getInstances())
                for (var modifier : inst.getModifiers())
                    collected.add(modifier.getKey());

            // Dump all
            list.addAll(collected);

        }, (explorer, input) -> input);
    }

    public static final Argument<String> COOLDOWN_CURRENT = new Argument<>("cooldown_key", (tree, list) -> {

        // Retrieve active cooldown keys if sender is player
        if (tree.getSender() instanceof Player) {
            final var playerData = MMOPlayerData.get((Player) tree.getSender());
            final var keys = playerData.getCooldownMap().getCooldownKeys();
            if (keys.isEmpty()) list.add("my_cooldown_key"); // Placeholder value
            else list.addAll(keys);
        }

        // Placeholder value
        else list.add("my_cooldown_key");
    }, (explorer, input) -> UtilityMethods.enumName(input));

    //endregion

    //region Deprecated

    @Deprecated
    @SuppressWarnings("rawtypes")
    public static final Argument PLAYER_OPTIONAL = PLAYER;

    @Deprecated
    @SuppressWarnings("rawtypes")
    public static final Argument AMOUNT_OPTIONAL = AMOUNT_INT;

    @Deprecated
    public Argument(String key, BiConsumer<CommandTreeExplorer, List<String>> autoComplete) {
        this(key, autoComplete, null, explorer -> null);
    }

    @Deprecated
    public Argument(@NotNull String key, boolean optional,
                    @NotNull BiConsumer<CommandTreeExplorer, List<String>> autoComplete) {
        this(key, autoComplete, null, explorer -> null);
    }

    @Deprecated
    public Argument(@NotNull String key, @Nullable Boolean optional, @NotNull BiConsumer<CommandTreeExplorer, List<String>> autoComplete) {
        this(key, autoComplete, null, explorer -> null);
    }

    //endregion
}
