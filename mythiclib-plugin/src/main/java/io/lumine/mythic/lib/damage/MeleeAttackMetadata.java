package io.lumine.mythic.lib.damage;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.provider.StatProvider;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Used by melee attacks with melee weapons like custom
 * or vanilla swords, axes...
 */
public class MeleeAttackMetadata extends AttackMetadata {

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
    public MeleeAttackMetadata(DamageMetadata damage, @NotNull LivingEntity target, @NotNull StatProvider attacker) {
        super(damage, target, attacker);
    }

    /**
     * @return Hand used in the action
     * @deprecated Useless
     */
    @Deprecated
    public EquipmentSlot getHand() {
        Validate.notNull(getAttacker(), "No attacker found");
        Validate.isTrue(getAttacker() instanceof PlayerMetadata, "Attacker is not a player");
        return ((PlayerMetadata) getAttacker()).getActionHand();
    }
}
