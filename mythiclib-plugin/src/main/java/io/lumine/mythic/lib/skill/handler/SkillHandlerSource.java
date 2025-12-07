package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class SkillHandlerSource {
    private final String key;
    private final BiFunction<ConfigurationSection, String, SkillHandler<?>> constructor;
    @BackwardsCompatibility(version = "1.7.1-SNAPSHOT", reason = "List of internal skill config paths used in previous MythicLib versions")
    private final List<String> legacyInternalSkillPaths;

    public SkillHandlerSource(@NotNull String key,
                              @NotNull BiFunction<ConfigurationSection, String, SkillHandler<?>> constructor) {
        this(key, constructor, List.of());
    }

    /**
     * @param key         Unique key for the skill handler type
     * @param constructor Function that provides the skill handler given the previous config,
     *                    if the config matches
     */
    @BackwardsCompatibility(version = "1.7.1-SNAPSHOT")
    public SkillHandlerSource(@NotNull String key,
                              @NotNull BiFunction<ConfigurationSection, String, SkillHandler<?>> constructor,
                              @NotNull List<String> legacyInternalSkillPaths) {
        this.key = Objects.requireNonNull(key, "Key cannot be null");
        this.legacyInternalSkillPaths = Objects.requireNonNull(legacyInternalSkillPaths, "Legacy internal skill paths cannot be null");
        this.constructor = Objects.requireNonNull(constructor, "Constructor cannot be null");
    }

    public String getKey() {
        return key;
    }

    public List<String> getLegacyInternalSkillPaths() {
        return legacyInternalSkillPaths;
    }

    public BiFunction<ConfigurationSection, String, SkillHandler<?>> getConstructor() {
        return constructor;
    }
}
