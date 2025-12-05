package io.lumine.mythic.lib.skill.handler;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.CoreToolsSkillResult;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ranet.coretools.scripts.casting.queue.ScriptQueue;

/**
 * A skill behaviour based on a custom CoreTools script
 */
public class CoreToolsSkillHandler extends SkillHandler<CoreToolsSkillResult> {

    // TODO avoid map lookup and cache script instead? or use persistent Lazy
    private final String scriptName;

    public static final String SOURCE = "mythiclib";

    public CoreToolsSkillHandler(ConfigurationSection config) {
        super(config);

        this.scriptName = config.getString("coretools-script-id");
    }

    @Override
    public @NotNull CoreToolsSkillResult getResult(SkillMetadata meta) {
        return new CoreToolsSkillResult(meta, scriptName);
    }

    @Override
    public void whenCast(CoreToolsSkillResult result, SkillMetadata skillMeta) {
        ScriptQueue.cast(scriptName, result.getContext(), SOURCE, false, true);
    }
}
