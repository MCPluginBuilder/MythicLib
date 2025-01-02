package io.lumine.mythic.lib.script.mechanic.buff;

import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;

@MechanicMetadata
public class FeedMechanic extends TargetMechanic {
    private final DoubleFormula amount;

    public FeedMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("amount");

        amount = new DoubleFormula(config.getString("amount"));
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Can only feed a player");

        Player player = (Player) target;
        FoodLevelChangeEvent called = new FoodLevelChangeEvent(player, (int) amount.evaluate(meta));
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return;

        player.setFoodLevel(Math.min(20, Math.max(0, called.getFoodLevel())));
    }
}
