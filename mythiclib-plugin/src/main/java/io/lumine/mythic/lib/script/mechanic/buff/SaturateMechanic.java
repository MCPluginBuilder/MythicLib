package io.lumine.mythic.lib.script.mechanic.buff;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@MechanicMetadata
public class SaturateMechanic extends TargetMechanic {
    private final DoubleFormula amount;
    // private final EntityRegainHealthEvent.RegainReason reason;

    public SaturateMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("amount");

        amount = new DoubleFormula(config.getString("amount"));
        // reason = EntityRegainHealthEvent.RegainReason.valueOf(UtilityMethods.enumName(config.getString("reason", "CUSTOM")));
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Can only give saturation to a player");

        Player player = (Player) target;
        player.setSaturation((float) Math.max(0, amount.evaluate(meta)));
    }
}
