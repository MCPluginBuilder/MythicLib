package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.MechanicQueue;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.MythicLibSkillResult;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A skill handler based on a custom MythicLib script
 */
public class MythicLibSkillHandler extends SkillHandler<MythicLibSkillResult> {
    private final Script script;

    public MythicLibSkillHandler(@NotNull ConfigurationSection config, @NotNull String scriptId) {
        this(config, MythicLib.plugin.getSkills().getScriptOrThrow(scriptId));
    }

    public MythicLibSkillHandler(@NotNull ConfigurationSection config, @NotNull Script script) {
        super(config);

        this.script = Objects.requireNonNull(script, "Script cannot be null");
    }

    public MythicLibSkillHandler(@NotNull Script script) {
        super((ConfigurationSection) null);

        this.script = script;
    }

    @Override
    public @NotNull MythicLibSkillResult getResult(SkillMetadata meta) {
        return new MythicLibSkillResult(meta, script);
    }

    @Override
    public void whenCast(MythicLibSkillResult result, SkillMetadata skillMeta) {
        new MechanicQueue(skillMeta, script).next();
    }
}
