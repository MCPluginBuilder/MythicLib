package io.lumine.mythic.lib.script.mechanic.offense;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

@MechanicMetadata
public class RemovePotionMechanic extends TargetMechanic {
    private final PotionEffectType effect;

    public RemovePotionMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("effect");

        effect = PotionEffectType.getByName(UtilityMethods.enumName(config.getString("effect")));
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof LivingEntity, "Cannot add a potion effect to a non living entity");
        ((LivingEntity) target).removePotionEffect(effect);
    }
}