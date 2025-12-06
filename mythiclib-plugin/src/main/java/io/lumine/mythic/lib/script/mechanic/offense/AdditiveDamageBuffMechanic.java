package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.entity.Entity;

@MechanicMetadata
@Deprecated
public class AdditiveDamageBuffMechanic extends TargetMechanic {
    private final NumericExpression amount;
    private final DamageType damageType;

    /**
     * @see MultiplyDamageMechanic
     * @see DamageMechanic
     * @deprecated
     */
    @Deprecated
    public AdditiveDamageBuffMechanic(ConfigObject config) {
        super(config);

        amount = config.numericExpr("amount");
        damageType = config.parse(null, Parsers.DAMAGE_TYPE, "damage_type", "dtype", "dt");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        if (damageType == null)
            meta.getAttackSource().getDamage().additiveModifier(amount.evaluate(meta));
        else
            meta.getAttackSource().getDamage().additiveModifier(amount.evaluate(meta), damageType);
    }
}
