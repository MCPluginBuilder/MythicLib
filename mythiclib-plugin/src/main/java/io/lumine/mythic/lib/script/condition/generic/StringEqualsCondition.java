package io.lumine.mythic.lib.script.condition.generic;

import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

/**
 * Checks if two strings are equal
 */
public class StringEqualsCondition extends Condition {
    private final String first, second;
    private final boolean ignoreCase;

    public StringEqualsCondition(ConfigObject config) {
        super(config);

        first = config.string("first", "one", "left", "lhs");
        second = config.string("second", "two", "right", "rhs");
        ignoreCase = config.bool(false, "ignore_case", "ic");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        return ignoreCase ?
                meta.parseString(first).equalsIgnoreCase(meta.parseString(second))
                : meta.parseString(first).equals(meta.parseString(second));
    }
}
