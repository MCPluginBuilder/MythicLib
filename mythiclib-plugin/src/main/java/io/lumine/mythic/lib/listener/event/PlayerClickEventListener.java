package io.lumine.mythic.lib.listener.event;

import io.lumine.mythic.lib.api.event.PlayerClickEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerDropItemEvent;
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
 * @see <a href="https://hub.spigotmc.org/jira/si/jira.issueviews:issue-html/SPIGOT-5632/SPIGOT-5632.html">...</a>
 * @see <a href="https://hub.spigotmc.org/jira/browse/SPIGOT-5435">...</a>
 * @see <a href="https://github.com/PaperMC/Paper/issues/3289">...</a>
 */
public class PlayerClickEventListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClick(PlayerInteractEvent event) {
        // Bukkit.broadcastMessage("PlayerInteract " + event.getClass().getSimpleName() + " " + event.getHand() + " " + event.getAction());

        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK: {
                var playerData = MMOPlayerData.getOrNull(event.getPlayer());

                // Prevent left clicks being registered twice, and prevent
                // right clicks causing arm swings being registered as left clicks.
                if (playerData != null) playerData.blockLeftClicks(25);

                Bukkit.getPluginManager().callEvent(new PlayerClickEvent(event.getPlayer(), Objects.requireNonNullElse(EquipmentSlot.fromBukkit(event.getHand()), EquipmentSlot.MAIN_HAND), false, event.getClickedBlock(), event.getItem(), event));
                break;
            }
        }
    }

    /**
     * Recent Spigot builds call an interact event on item drops,
     * when pressing Q or from inside an inventory UI. This only happens
     * when the player is looking at a block.
     * <p>
     * Simple implementation of a N-ms timeout after drop events
     * where all interact events are ignored.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeftClick(PlayerDropItemEvent event) {
        //  System.out.println("PlayerDropItem " + event.getClass().getSimpleName());

        var playerData = MMOPlayerData.getOrNull(event.getPlayer());

        // Prevent drops being registered as left clicks
        if (playerData != null) playerData.blockLeftClicks(25);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeftClick(PlayerAnimationEvent event) {
        //  Bukkit.broadcastMessage("PlayerAnimation " + event.getClass().getSimpleName() + " " + event.getAnimationType());

        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
            var playerData = MMOPlayerData.getOrNull(event.getPlayer());

            // Prevent left clicks being registered twice, and prevent
            // right clicks causing arm swings being registered as left clicks.
            if (playerData != null && !playerData.canLeftClick()) return;

            Bukkit.getPluginManager().callEvent(new PlayerClickEvent(event.getPlayer(), EquipmentSlot.MAIN_HAND, true, null, event.getPlayer().getInventory().getItemInMainHand(), event));

            // Theoretically, players can spam to get 1 right click per 1-2 ticks
            // though spamming left clicks is much easier (hold down left click on
            // a block). To mitigate this, MythicLib avoids calling more than 1 left
            // click every 2 ticks.
            if (playerData != null) playerData.blockLeftClicks(75);
        }
    }

    // @EventHandler(priority = EventPriority.LOWEST)
    // public void debugClicks(PlayerClickEvent event) {
    //     Bukkit.broadcastMessage("PlayerClick " + event.getClass().getSimpleName());
    // }
}
