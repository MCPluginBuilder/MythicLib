package io.lumine.mythic.lib.damage;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.provider.PlayerStatProvider;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Instanced every time MythicLib detects and monitors an attack from any player.
 * <p>
 * TODO remove multiple instances of AttackMeta and make them instances of DamagePacket
 * TODO Better to reduce confusion. It makes more sense to have MythicLib fully control
 * TODO types of AttackMeta but have other plugins able to implement other types
 * TODO of damage packets.
 *
 * @author Indyuce
 */
public class AttackMetadata {

    @NotNull
    private final DamageMetadata damage;

    /**
     * This field can actually be null but this is only for
     * backwards compatibility and will be modified in a distant future.
     */
    @NotNull
    private final LivingEntity target;

    @Nullable
    private final StatProvider attacker;

    /**
     * @deprecated Attack metas with no targets are deprecated
     */
    @Deprecated
    public AttackMetadata(@NotNull DamageMetadata damage, @Nullable StatProvider attacker) {
        this(damage, null, attacker);
    }

    /**
     * Used by AttackHandler instances to register attacks. AttackResult only
     * gives information about the attack damage and types while this class also
     * contains info about the damager. Some plugins don't let MythicLib determine
     * what the damager is so there might be problem with damage/reduction stat
     * application.
     *
     * @param damage   The attack result
     * @param attacker The entity who dealt the damage
     */
    public AttackMetadata(@NotNull DamageMetadata damage, @NotNull LivingEntity target, @Nullable StatProvider attacker) {
        this.attacker = attacker;
        this.target = target;
        this.damage = Objects.requireNonNull(damage, "Damage cannot be null");
    }

    /**
     * @return Information about the attack damage
     */
    @NotNull
    public DamageMetadata getDamage() {
        return damage;
    }

    @NotNull
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * @return The corresponding PlayerMetadata if the attacker is
     *         a player, null otherwise
     */
    @Nullable
    public StatProvider getAttacker() {
        return attacker;
    }

    public boolean hasAttacker() {
        return attacker != null;
    }

    /**
     * @return If this is a player attack
     */
    public boolean isPlayer() {
        return attacker != null && attacker instanceof PlayerStatProvider;
    }

    /**
     * @return Whether or not the corresponding attack is closed.
     * @deprecated Expiration is no longer well defined since AttackMetadatas
     *         are cleaned right after damage application.
     */
    @Deprecated
    public boolean hasExpired() {
        return false;
    }

    /**
     * @deprecated Expiration is no longer well defined since AttackMetadatas
     *         are cleaned right after damage application.
     */
    @Deprecated
    public void expire() {
        // Nothing
    }

    /**
     * @deprecated Cloning is now ambiguous because no target entity is specified. Please use the
     *         new constructor/{@link PlayerMetadata#attack(LivingEntity, double, DamageType...)} instead of cloning
     */
    @Deprecated
    public AttackMetadata clone() {
        return new AttackMetadata(damage.clone(), target, attacker);
    }

    /**
     * @deprecated There is no longer such a method in the AttackMetadata class.
     *         Use {@link PlayerMetadata#attack(LivingEntity, double, DamageType...)} instead to have a player
     *         deal damage.
     */
    @Deprecated
    public void damage(LivingEntity target) {
        damage(target, true);
    }

    /**
     * @deprecated There is no longer such a method in the AttackMetadata class.
     *         Use {@link PlayerMetadata#attack(LivingEntity, double, DamageType...)} instead to have a player
     *         deal damage.
     */
    @Deprecated
    public void damage(LivingEntity target, boolean knockback) {
        MythicLib.plugin.getDamage().damage(this, target, knockback);
    }

    /**
     * @deprecated AttackMetadata no longer extends PlayerMetadata
     */
    @Deprecated
    public Player getPlayer() {
        Validate.notNull(attacker, "No attacker was found");
        Validate.isTrue(attacker instanceof PlayerMetadata, "Attacker is not a player");
        return ((PlayerMetadata) attacker).getPlayer();
    }

    /**
     * @deprecated AttackMetadata no longer extends PlayerMetadata
     */
    @Deprecated
    public MMOPlayerData getData() {
        Validate.notNull(attacker, "No attacker was found");
        Validate.isTrue(attacker instanceof PlayerMetadata, "Attacker is not a player");
        return ((PlayerMetadata) attacker).getData();
    }

    /**
     * @deprecated AttackMetadata no longer extends PlayerMetadata
     */
    @Deprecated
    public double getStat(String stat) {
        Validate.notNull(attacker, "No attacker was found");
        return attacker.getStat(stat);
    }

    /**
     * @deprecated AttackMetadata no longer extends PlayerMetadata
     */
    @Deprecated
    public void setStat(String stat, double value) {
        Validate.notNull(attacker, "No attacker was found");
        Validate.isTrue(attacker instanceof PlayerMetadata, "Attacker is not a player");
        ((PlayerMetadata) attacker).setStat(stat, value);
    }

    /**
     * @deprecated AttackMetadata no longer extends PlayerMetadata
     */
    @Deprecated
    public AttackMetadata attack(LivingEntity target, double damage, DamageType... types) {
        Validate.notNull(attacker, "No attacker was found");
        Validate.isTrue(attacker instanceof PlayerMetadata, "Attacker is not a player");
        return ((PlayerMetadata) attacker).attack(target, damage, types);
    }

    /**
     * @deprecated AttackMetadata no longer extends PlayerMetadata
     */
    @Deprecated
    public AttackMetadata attack(LivingEntity target, double damage, boolean knockback, DamageType... types) {
        Validate.notNull(attacker, "No attacker was found");
        Validate.isTrue(attacker instanceof PlayerMetadata, "Attacker is not a player");
        return ((PlayerMetadata) attacker).attack(target, damage, knockback, types);
    }
}
