package io.lumine.mythic.lib.api.stat.provider;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used to generalize stat maps to both players and non-player
 * entities. Some statistics should apply even if the target entity is NOT
 * a player (in that case, you can't just get the MMOPlayerData and then
 * the stat map).
 * <p>
 * TODO Transform this into EntityMetadata and merge it with PlayerMetadata. Good for GUI script centralization
 *
 * @author Jules
 * @see EntityStatProvider
 */
public interface StatProvider {
    double getStat(String stat);

    @NotNull
    LivingEntity getEntity();

    @NotNull
    EquipmentSlot getActionHand();

    @Deprecated
    static StatProvider get(LivingEntity living) {
        return get(living, EquipmentSlot.MAIN_HAND, true);
    }

    @Deprecated
    static StatProvider generate(LivingEntity living, EquipmentSlot actionHand) {
        return get(living, actionHand, true);
    }

    /**
     * @param living     Living entity
     * @param actionHand Hand used to perform the action
     * @param cache      If the entity is a player, their stats should be cached.
     *                   It can be useless to do that because
     * @return The stat provider of the corresponding entity
     */
    @NotNull
    static StatProvider get(LivingEntity living, EquipmentSlot actionHand, boolean cache) {
        if (!UtilityMethods.isRealPlayer(living)) return new EntityStatProvider(living);

        final Player player = (Player) living;
        final StatMap statMap = MMOPlayerData.get(player).getStatMap();
        return cache ? statMap.cache(actionHand) : statMap;
    }
}
