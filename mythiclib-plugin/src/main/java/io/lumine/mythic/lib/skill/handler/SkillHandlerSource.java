package io.lumine.mythic.lib.skill.handler;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class SkillHandlerSource {
    private final String key;
    private final Predicate<ConfigurationSection> matcher;
    private final Function<ConfigurationSection, SkillHandler<?>> constructor;
    private final Function<String, SkillHandler<?>> skillFetcher;

    /**
     * @param key         Unique key for the skill handler type
     * @param matcher     If a certain skill config redirects to the skill handler
     *                    Example: a config which the following key should be handled
     *                    by {@link io.lumine.mythic.lib.skill.handler.MythicMobsSkillHandler}
     *                    <code>mythic-mobs-skill-id: WarriorStrike</code>
     * @param constructor Function that provides the skill handler given the previous config,
     *                    if the config matches
     */
    public SkillHandlerSource(@NotNull String key,
                              @NotNull Predicate<ConfigurationSection> matcher,
                              @NotNull Function<ConfigurationSection, SkillHandler<?>> constructor,
                              @NotNull Function<String, SkillHandler<?>> skillFetcher) {
        this.key = Objects.requireNonNull(key, "Key cannot be null");
        this.matcher = Objects.requireNonNull(matcher, "Matcher cannot be null");
        this.constructor = Objects.requireNonNull(constructor, "Constructor cannot be null");
        this.skillFetcher = Objects.requireNonNull(skillFetcher, "Skill fetcher cannot be null");
    }

    public String getKey() {
        return key;
    }

    public Predicate<ConfigurationSection> getMatcher() {
        return matcher;
    }

    public Function<ConfigurationSection, SkillHandler<?>> getConstructor() {
        return constructor;
    }

    public Function<String, SkillHandler<?>> getSkillFetcher() {
        return skillFetcher;
    }
}
