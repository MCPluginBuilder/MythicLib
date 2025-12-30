package io.lumine.mythic.lib.skill.result;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.CoreToolsSkillHandler;
import io.lumine.mythic.lib.util.Lazy;
import org.jetbrains.annotations.NotNull;
import ranet.coretools.scripts.casting.Context;
import ranet.coretools.scripts.casting.queue.ScriptQueue;

public class CoreToolsSkillResult implements SkillResult {
    private final Lazy<Boolean> conditionsMet;
    private final Context context;

    public CoreToolsSkillResult(@NotNull SkillMetadata skillMeta, @NotNull String scriptName) {
        this.context = adaptToContext(skillMeta);
        this.conditionsMet = Lazy.of(() -> ScriptQueue.cast(scriptName, context, CoreToolsSkillHandler.CORETOOLS_SOURCE, true, false));

        // Store skillMetadata inside CoreTools context
        context.setMythiclib_meta(skillMeta);
    }

    private Context adaptToContext(SkillMetadata skillMeta) {

        var context = new Context(skillMeta.getCaster().getPlayer());
        context.setTarget(skillMeta.getTargetEntityOrNull());
        context.setTrigger(skillMeta.getTargetEntityOrNull()); // Trigger and target
        context.setLocation(skillMeta.getSourceLocation());
        context.setTargetlocation(skillMeta.getTargetLocationOrNull());

        return context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public boolean isSuccessful() {
        return conditionsMet.get();
    }
}
