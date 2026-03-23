package io.lumine.mythic.lib.script.mechanic.misc;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Entity;

import java.util.function.Function;

@MechanicMetadata
public class EntityEffectMechanic extends TargetMechanic {
    private final EntityEffect effect;

    public static final Function<String, EntityEffect> PARSER_ENTITY_EFFECT = Parsers.ofEnum(EntityEffect.class, EntityEffect::valueOf);

    public EntityEffectMechanic(ConfigObject config) {
        super(config);

        effect = config.parse(PARSER_ENTITY_EFFECT, "effect");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        target.playEffect(effect);
    }
}
