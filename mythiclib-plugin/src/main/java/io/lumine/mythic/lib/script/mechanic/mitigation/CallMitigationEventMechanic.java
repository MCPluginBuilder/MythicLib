package io.lumine.mythic.lib.script.mechanic.mitigation;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.mitigation.MitigationType;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.jetbrains.annotations.NotNull;

public class CallMitigationEventMechanic extends Mechanic {
    private final Script callback;
    private final MitigationType mitigationType;
    private final String resultVariableName;

    public CallMitigationEventMechanic(ConfigObject config) {
        callback = config.script("callback", "cb");
        resultVariableName = config.string("result_variable", "rv", "return_value", "ret", "retval", "ret_val");
        var mitigationMechanicName = config.string("mechanic", "id", "name", "n");
        mitigationType = MythicLib.plugin.getMitigation().getMitigationType(mitigationMechanicName);
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {

    }
}
