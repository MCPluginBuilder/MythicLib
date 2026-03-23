package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@MechanicMetadata
public class PotionMechanic extends TargetMechanic {
    private final PotionEffectType effect;
    private final NumericExpression duration, level;
    private final boolean ambient, particles, icon;

    public PotionMechanic(ConfigObject config) {
        super(config);

        effect = config.parse(Parsers.POTION_EFFECT_TYPE, "effect", "eff", "e", "type", "pe");
        level = config.numericExpr(NumericExpression.ONE, "level", "lvl", "l");
        duration = config.numericExpr("ticks", "t", "duration", "dur", "d", "time");
        ambient = config.bool(false, "ambient", "amb");
        particles = config.bool(true, "particles", "part");
        icon = config.bool(true, "icon", "ic");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof LivingEntity, "Cannot add a potion effect to a non living entity");

        ((LivingEntity) target).addPotionEffect(new PotionEffect(effect, (int) duration.evaluate(meta), (int) level.evaluate(meta), ambient, particles, icon));
    }
}