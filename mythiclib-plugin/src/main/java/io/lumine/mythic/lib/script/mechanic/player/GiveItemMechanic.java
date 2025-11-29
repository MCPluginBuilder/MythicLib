package io.lumine.mythic.lib.script.mechanic.player;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveItemMechanic extends TargetMechanic {
    private final Material material;
    private final DoubleFormula amount;

    public GiveItemMechanic(ConfigObject config) {
        super(config);

        material = UtilityMethods.prettyValueOf(Material::valueOf, config.string("material", "mat", "m"), "No material with ID %s");
        amount = config.getDoubleFormula(DoubleFormula.constant(1), "amount", "amt", "a", "count", "cnt", "c", "number", "num", "nb", "n");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Target is not a player");
        int amount = (int) this.amount.evaluate(meta);
        amount = Math.max(0, amount);
        ((Player) target).getInventory().addItem(new ItemStack(material, amount));
    }
}
