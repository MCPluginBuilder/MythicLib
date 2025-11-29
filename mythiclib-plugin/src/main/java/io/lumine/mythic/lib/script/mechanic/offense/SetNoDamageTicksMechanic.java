package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

@MechanicMetadata
public class SetNoDamageTicksMechanic extends TargetMechanic {
    private final DoubleFormula ticks;

    public SetNoDamageTicksMechanic(ConfigObject config) {
        super(config);

        ticks = config.getDoubleFormula(DoubleFormula.constant(10), "ticks", "t", "duration", "dur", "d", "time");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof LivingEntity, "SetNoDamageTicksMechanic can only be applied to living entities");

        final var ticks = (int) this.ticks.evaluate(meta);
        Validate.isTrue(ticks >= 0, "NoDamageTicks duration must be non-negative");

        ((LivingEntity) target).setNoDamageTicks(ticks);
    }
}
