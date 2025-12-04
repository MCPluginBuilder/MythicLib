package io.lumine.mythic.lib.script.condition.generic;

import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Checks if two strings are equal
 */
public class StringContainsCondition extends Condition {
    private final String first, second;
    private final boolean ignoreCase;

    public StringContainsCondition(ConfigObject config) {
        super(config);

        first = config.string("search", "look", "lookfor", "lf");
        second = config.string("in", "within");
        ignoreCase = config.bool(false, "ignore_case", "ic");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        return ignoreCase ?
                meta.parseString(first).toLowerCase().contains(meta.parseString(second).toLowerCase())
                : meta.parseString(first).contains(meta.parseString(second));
    }
}
