package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@MechanicMetadata
public class PotionMechanic extends TargetMechanic {
    private final PotionEffectType effect;
    private final DoubleFormula duration, level;
    private final boolean ambient, particles, icon;

    public PotionMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("effect", "duration");

        effect = PotionEffectType.getByName(UtilityMethods.enumName(config.getString("effect")));
        level = new DoubleFormula(config.getString("level", "1"));
        duration = new DoubleFormula(config.getString("duration"));
        ambient = config.getBoolean("ambient", true);
        particles = config.getBoolean("particles", true);
        icon = config.getBoolean("icon", true);
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof LivingEntity, "Cannot add a potion effect to a non living entity");

        ((LivingEntity) target).addPotionEffect(new PotionEffect(effect, (int) duration.evaluate(meta), (int) level.evaluate(meta), ambient, particles, icon));
    }
}