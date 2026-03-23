package io.lumine.mythic.lib.script.mechanic.buff.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.api.stat.modifier.TemporaryStatModifier;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.TargetMechanic;
import io.lumine.mythic.lib.script.util.expression.numeric.NumericExpression;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AddStatModifierMechanic extends TargetMechanic {
    private final NumericExpression amount, lifetime;
    private final String stat, key;
    private final boolean relative, unique;

    @MechanicMetadata
    public AddStatModifierMechanic(ConfigObject config) {
        super(config);

        stat = config.string("stat");
        key = config.stringFb("default", "key", "k");
        lifetime = config.numericExpr(NumericExpression.ZERO, "time", "duration", "dur", "d", "ticks", "t");
        relative = config.getBoolean("relative", false);
        amount = config.numericExpr("amount", "a", "value", "v");
        unique = config.bool("unique", "u");
    }

    @Override
    public void cast(SkillMetadata meta, Entity target) {
        Validate.isTrue(target instanceof Player, "Can only give temporary stats to players");

        MMOPlayerData playerData = MMOPlayerData.get((OfflinePlayer) target);
        long lifetime = Math.max(0, (long) this.lifetime.evaluate(meta));
        final var uniqueId = unique ? UtilityMethods.uniqueIdFromString(this.key) : UUID.randomUUID();

        if (lifetime > 0)
            new TemporaryStatModifier(uniqueId, key, stat, amount.evaluate(meta), relative ? ModifierType.RELATIVE : ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData, lifetime);
        else
            new StatModifier(uniqueId, key, stat, amount.evaluate(meta), relative ? ModifierType.RELATIVE : ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER).register(playerData);
    }
}
