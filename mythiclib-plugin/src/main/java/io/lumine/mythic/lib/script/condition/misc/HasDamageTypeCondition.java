package io.lumine.mythic.lib.script.condition.misc;

import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

import java.util.List;

/**
 * Checks if the attackMeta bound to the skillMeta has some damage type.
 * This condition can only be used when using a trigger type like DAMAGED or DAMAGE
 * <p>
 * This checks if the attack has at least one of the damage types provided.
 */
public class HasDamageTypeCondition extends Condition {
    private final List<DamageType> types;

    public HasDamageTypeCondition(ConfigObject config) {
        super(config);

        types = config.parse(Parsers.DAMAGE_TYPES, "types");
    }

    @Override
    public boolean isMet(SkillMetadata meta) {
        final DamageMetadata damage = meta.getAttackSource().getDamage();
        for (DamageType checked : types)
            if (damage.hasType(checked))
                return true;

        return false;
    }
}