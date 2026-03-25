package io.lumine.mythic.lib.listener.event;

import io.lumine.mythic.lib.api.event.PlayerClickEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

/**
 * This fixes a well known issue with vanilla Spigot.
 * <p>
 * Spigot <=1.20.5 Left-Click Behavior:
 * - distance < 3, successful melee hit, no interact event but attack event.
 * - distance 3 - 5, missed melee attack, no interact event, no attack event.
 * - distance > 5, not an attack, interact event.
 * - at any distance, swing event.
 * <p>
 * Spigot >1.20.5 Left-Click Behavior:
 * - distance <= entity_interaction_range, successful melee hit, no interact event, attack event
 * - distance > entity_interaction_range, failed melee hit, interact event
 * <p>
 * Technically, only the part between 3 and 5 blocks in <=1.20.5 is problematic,
 * as PlayerInteractEvent's might be designed to only trigger when out-of-range.
 * This means that this class DOES MODIFY the specification of PlayerInteractEvent,
 * which could lead to issues with other plugins.
 * <p>
 * The old fix MythicLib used to do was to try and guess if Spigot were to
 * send an interact packet, and send a spoofed interact event if it had
 * guessed none would be called. This solution 1/ broke many times when
 * switching versions (hard to predict), 2/ had the risk of introducing
 * dupe events, 3/ changed the Bukkit event specification.
 * <p>
 * MythicLib now introduces a custom {@link PlayerClickEvent} event called
 * on PlayerAnimationEvent's for left clicks, and PlayerInteractEvents for
 * right clicks. It no longer changes the Bukkit event specification.
 *
 * @author jules
 * @see <a href="https://www.spigotmc.org/threads/1-19-playerinteractevent-not-called-when-entity-is-in-sight-client-bug.574671/">...</a>
 * @see <a href="https://github.com/PluginBugs/Issues-ItemsAdder/issues/1993">...</a>
 * @see <a href="https://www.spigotmc.org/threads/detect-when-a-player-left-clicks-an-entity.603228/">...</a>
 */
public class PlayerClickEventListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            var playerData = MMOPlayerData.getOrNull(event.getPlayer());
            if (playerData != null) playerData.nextLeftClick = System.currentTimeMillis() + 25;
            Bukkit.getPluginManager().callEvent(new PlayerClickEvent(event.getPlayer(), Objects.requireNonNullElse(EquipmentSlot.fromBukkit(event.getHand()), EquipmentSlot.MAIN_HAND), false));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeftClick(PlayerAnimationEvent event) {
        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
            var playerData = MMOPlayerData.getOrNull(event.getPlayer());
            if (playerData != null && playerData.nextLeftClick > System.currentTimeMillis()) return;
            Bukkit.getPluginManager().callEvent(new PlayerClickEvent(event.getPlayer(), EquipmentSlot.MAIN_HAND, true));

            // Theoretically, players can spam to get 1 right click per 1-2 ticks
            // though spamming left clicks is much easier (hold down left click on
            // a block). To mitigate this, MythicLib avoids calling more than 1 left
            // click every 2 ticks.
            if (playerData != null) playerData.nextLeftClick = System.currentTimeMillis() + 75;
        }
    }
}
