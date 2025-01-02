package io.lumine.mythic.lib.script.mechanic.buff.stat;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.api.stat.modifier.TemporaryStatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class AddStatModifierMechanic extends TargetMechanic {
    private final DoubleFormula amount, lifetime;
    private final String stat, key;
    private final boolean relative;

    @MechanicMetadata
    public AddStatModifierMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("stat", "amount", "key");

        stat = config.getString("stat");
        key = config.getString("key");
        lifetime = config.contains("time") ? new DoubleFormula(config.getString("time")) : DoubleFormula.ZERO;
        relative = config.getBoolean("relative", false);
        amount = new DoubleFormula(config.getString("amount"));
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Can only give temporary stats to players");

        MMOPlayerData playerData = MMOPlayerData.get((OfflinePlayer) target);
        long lifetime = Math.max(0, (long) this.lifetime.evaluate(meta));

        if (lifetime > 0)
            new TemporaryStatModifier(key, stat, amount.evaluate(meta), relative ? ModifierType.RELATIVE : ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData, lifetime);
        else
            new StatModifier(key, stat, amount.evaluate(meta), relative ? ModifierType.RELATIVE : ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData);
    }
}
