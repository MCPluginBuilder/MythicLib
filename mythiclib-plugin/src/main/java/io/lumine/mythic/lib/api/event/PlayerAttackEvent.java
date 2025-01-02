package io.lumine.mythic.lib.api.event;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.provider.PlayerStatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An attack that is called by a player
 */
public class PlayerAttackEvent extends AttackEvent implements Cancellable {
    private final PlayerMetadata attacker;

    /**
     * Called whenever a player deals damage to another entity.
     *
     * @param event  The corresponding damage event
     * @param attack The generated attack result which can be edited
     */
    public PlayerAttackEvent(EntityDamageEvent event, AttackMetadata attack) {
        super(event, attack);

        Validate.isTrue(attack.isPlayer(), "Not a player attack");
        this.attacker = ((PlayerStatProvider) attack.getAttacker()).cache(EquipmentSlot.MAIN_HAND);
    }

    @NotNull
    public PlayerMetadata getAttacker() {
        return attacker;
    }

    /**
     * @deprecated PlayerAttackEvent no longer extends PlayerEvent
     */
    @Deprecated
    public MMOPlayerData getData() {
        return attacker.getData();
    }

    /**
     * @deprecated PlayerAttackEvent no longer extends PlayerEvent
     */
    @Deprecated
    public Player getPlayer() {
        return attacker.getPlayer();
    }
}

