package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

// TODO softcode the rest of this when implementing custom damage types
// it is possible to softcode this using on-hit effects but not
// 1) very performance friendly and 2) conventional/makes a lot of sense
@Deprecated
public class LegacyAttackEffects implements Listener {

    /**
     * On priority HIGH so that it applies onto elemental damage
     * which is applied on priority NORMAL.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHitAttackEffects(PlayerAttackEvent event) {
        var stats = event.getAttacker();

        // Apply specific damage increase
        for (var type : DamageType.values())
            event.getDamage().additiveModifier(stats.getStat(type.getOffenseStat()) / 100, type);

        // Apply undead damage
        if (UtilityMethods.isUndead(event.getEntity()))
            event.getDamage().additiveModifier(stats.getStat("UNDEAD_DAMAGE") / 100);

        // Apply PvP or PvE damage, one of the two anyways.
        event.getDamage().additiveModifier(stats.getStat(event.getEntity() instanceof Player ? "PVP_DAMAGE" : "PVE_DAMAGE") / 100);
    }
}
