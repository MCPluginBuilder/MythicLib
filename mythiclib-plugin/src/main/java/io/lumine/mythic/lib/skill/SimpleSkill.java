package io.lumine.mythic.lib.skill;

import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.handler.MythicLibSkillHandler;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class SimpleSkill extends Skill {
    private final SkillHandler<?> handler;

    public SimpleSkill(@NotNull SkillHandler<?> handler) {
        this.handler = handler;
    }

    public SimpleSkill(@NotNull Script script) {
        this(new MythicLibSkillHandler(script));
    }

    @Override
    public boolean getResult(@NotNull SkillMetadata skillMeta) {
        return true;
    }

    @Override
    public void whenCast(@NotNull SkillMetadata skillMeta) {
        // Nothing here
    }

    @Override
    public double getParameter(String path) {
        return 0;
    }

    @Override
    @NotNull
    public SkillHandler<?> getHandler() {
        return handler;
    }

    //region Deprecated

    @Deprecated
    public SimpleSkill(TriggerType triggerType, SkillHandler<?> handler) {
        this(handler);
    }

    @Deprecated
    public static final SimpleSkill EMPTY = new SimpleSkill(new SkillHandler<>(new YamlConfiguration()) {

        @Override
        public @NotNull SkillResult getResult(SkillMetadata meta) {
            return () -> true;
        }

        @Override
        public void whenCast(SkillResult result, SkillMetadata skillMeta) {
            // Nothing
        }
    });

    //endregion
}
