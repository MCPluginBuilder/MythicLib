package io.lumine.mythic.lib.script.condition.misc;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Checks if the caster world is XX
 */
public class PermissionCondition extends Condition {
    private final String permNode;

    public PermissionCondition(ConfigObject config) {
        super(config);

        permNode = config.getString("name");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        return meta.getCaster().getPlayer().hasPermission(permNode);
    }
}