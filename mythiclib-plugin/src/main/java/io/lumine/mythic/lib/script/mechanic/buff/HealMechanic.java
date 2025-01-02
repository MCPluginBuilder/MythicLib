package io.lumine.mythic.lib.script.mechanic.buff;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;

@MechanicMetadata
public class HealMechanic extends TargetMechanic {
    private final DoubleFormula amount;
    private final EntityRegainHealthEvent.RegainReason reason;

    public HealMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("amount");

        amount = new DoubleFormula(config.getString("amount"));
        reason = EntityRegainHealthEvent.RegainReason.valueOf(UtilityMethods.enumName(config.getString("reason", "CUSTOM")));
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Damageable, "Cannot heal a non living entity");
        LivingEntity living = (LivingEntity) target;

        EntityRegainHealthEvent called = new EntityRegainHealthEvent(target, amount.evaluate(meta), reason);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return;

        double maxHealth = living.getAttribute(Attributes.MAX_HEALTH).getValue();
        living.setHealth(Math.max(0, Math.min(maxHealth, living.getHealth() + called.getAmount())));
    }
}
