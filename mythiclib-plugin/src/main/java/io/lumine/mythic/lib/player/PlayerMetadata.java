package io.lumine.mythic.lib.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.provider.PlayerStatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A class representing a player at a specific point in time. It is
 * instanced when the player performs an action, and saves both the
 * hand which is performing the action (action hand) and a snapshot
 * of the full player stat map.
 *
 * @author jules
 */
public class PlayerMetadata implements PlayerStatProvider {
    private final Player player;
    private final MMOPlayerData playerData;
    private final Map<String, Double> playerStats;
    private final EquipmentSlot actionHand;

    public PlayerMetadata(PlayerMetadata parent) {
        Validate.notNull(parent, "Parent cannot be null");

        this.player = parent.player;
        this.playerData = parent.playerData;
        this.playerStats = parent.playerStats;
        this.actionHand = parent.actionHand;
    }

    public PlayerMetadata(StatMap statMap, @NotNull EquipmentSlot actionHand) {
        this.player = statMap.getData().getPlayer();
        this.playerData = statMap.getData();
        this.playerStats = new HashMap<>();
        this.actionHand = actionHand;

        Validate.isTrue(Objects.requireNonNull(actionHand).isHand(), "Equipment slot must be a hand");

        // Isolate stat modifiers
        for (StatInstance ins : statMap.getInstances())
            this.playerStats.put(ins.getStat(), ins.getFilteredTotal(actionHand::isCompatible));
    }

    @NotNull
    public MMOPlayerData getData() {
        return playerData;
    }

    @NotNull
    public EquipmentSlot getActionHand() {
        return actionHand;
    }

    /**
     * @param stat The string key of the stat
     * @return The cached stat value, or the vanilla
     */
    @Override
    public double getStat(String stat) {
        return playerStats.getOrDefault(stat, playerData.getStatMap().getInstance(stat).getBase());
    }

    /**
     * Edits the current cached stat value
     *
     * @param stat  The string key of the stat
     * @param value The value you want to cache
     */
    public void setStat(String stat, double value) {
        playerStats.put(stat, value);
    }

    /**
     * Utility method that makes a player deal damage to a specific
     * entity. This creates the attackMetadata based on the data
     * stored by the CasterMetadata, and calls it using MythicLib
     * damage manager
     *
     * @param target Target entity
     * @param damage Damage dealt
     * @param types  Type of target
     * @return The (modified) attack metadata
     */
    @NotNull
    public AttackMetadata attack(LivingEntity target, double damage, DamageType... types) {
        return attack(target, damage, true, types);
    }

    /**
     * Utility method that makes a player deal damage to a specific
     * entity. This creates the attackMetadata based on the data
     * stored by the CasterMetadata, and calls it using MythicLib
     * damage manager
     *
     * @param target    Target entity
     * @param damage    Damage dealt
     * @param knockback Should this attack apply knockback
     * @param types     Type of target
     * @return The (modified) attack metadata
     */
    @NotNull
    public AttackMetadata attack(LivingEntity target, double damage, boolean knockback, DamageType... types) {

        // Check if entity is not already being damaged
        final @Nullable AttackMetadata opt = MythicLib.plugin.getDamage().getRegisteredAttackMetadata(target);
        if (opt != null) {
            opt.getDamage().add(damage, types);
            return opt;
        }

        final AttackMetadata attackMeta = new AttackMetadata(new DamageMetadata(damage, types), target, this);
        MythicLib.plugin.getDamage().registerAttack(attackMeta, knockback, false);
        return attackMeta;
    }

    public void triggerSkills(TriggerType triggerType, @Nullable Entity entity, @Nullable AttackMetadata attack) {
        playerData.triggerSkills(new TriggerMetadata(this, triggerType, entity, attack));
    }

    @Override
    public @NotNull PlayerMetadata cache(@NotNull EquipmentSlot castHand) {
        // Data is already cached
        return this;
    }
}
