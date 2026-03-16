package io.lumine.mythic.lib.api.stat.provider;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
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
     * @param snapshot   This is a don't-care for non player entities.
     *                   A value of true will resulting in MythicLib taking a
     *                   snapshot at the current time of the player's current stat
     *                   values. If the player's stats change later due to a newly
     *                   held item, registered or unregistered modifier... then the
     *                   SkillMetadata will still inherit from the player's stats
     *                   at the time the skill was cast.
     * @return The stat provider of the corresponding entity, possibly a stat snapshot.
     */
    @NotNull
    static StatProvider get(@NotNull LivingEntity living, @NotNull EquipmentSlot actionHand, boolean snapshot) {
        if (!UtilityMethods.isRealPlayer(living)) return new EntityStatProvider(living);

        var player = (Player) living;
        var statMap = MMOPlayerData.get(player).getStatMap();
        return snapshot ? statMap.cache(actionHand) : statMap;
    }
}
