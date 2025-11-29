package io.lumine.mythic.lib.skill;

import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class EmptySkillHandler extends SkillHandler<SkillResult> {

    @Deprecated
    public EmptySkillHandler() {
        super("empty");
    }

    @Override
    public @NotNull SkillResult getResult(SkillMetadata meta) {
        return new CustomSkillResult();
    }

    @Override
    public void whenCast(SkillResult result, SkillMetadata skillMeta) {
        // Nothing here
    }

    @Deprecated
    static class CustomSkillResult implements SkillResult {
        public boolean isSuccessful() {
            return true;
        }
    }
}
