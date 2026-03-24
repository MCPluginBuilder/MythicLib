package io.lumine.mythic.lib.listener.option;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.event.FixPlayerInteractEvent;
import io.lumine.mythic.lib.version.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

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
 * This class fixes when the entity is at distance <5 to send an interact
 * event in this situation. The obtained behaviour is that interact events
 * are called EVERYTIME, even on successful melee hits.
 * <p>
 * Technically, only the part between 3 and 5 blocks in <=1.20.5 is problematic,
 * as PlayerInteractEvent's might be designed to only trigger when out-of-range.
 * This means that this class DOES MODIFY the specification of PlayerInteractEvent,
 * which could lead to issues with other plugins.
 *
 * @author jules
 * @see <a href="https://www.spigotmc.org/threads/1-19-playerinteractevent-not-called-when-entity-is-in-sight-client-bug.574671/">...</a>
 * @see <a href="https://github.com/PluginBugs/Issues-ItemsAdder/issues/1993">...</a>
 * @see <a href="https://www.spigotmc.org/threads/detect-when-a-player-left-clicks-an-entity.603228/">...</a>
 */
public class LeftClickEventFix implements Listener {
    protected final boolean legacyInteractionRange;

    /**
     * Max range used for <=1.20.5 before 'Entity Interaction Range' was implemented
     */
    protected static final double LEGACY_MAX_RANGE = 5;

    public LeftClickEventFix() {
        legacyInteractionRange = MythicLib.plugin.getVersion().isUnder(1, 20, 5);
    }

    @EventHandler
    public void onAnimationPlay(PlayerAnimationEvent event) {
        if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
            if (!eventCalled(event.getPlayer())) triggerEvent(event.getPlayer());
        }
    }

    /**
     * Approximate re-implementation of the following logic:
     * - raycast from the player towards their eye location
     * - is there an interact event being called
     *
     * @return If an event is expected to be called
     */
    private boolean eventCalled(Player player) {
        double entityInteractionRange = legacyInteractionRange ? LEGACY_MAX_RANGE : player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).getValue();

        RayTraceResult result = player.getWorld().rayTrace(player.getEyeLocation(), player.getEyeLocation().getDirection(), entityInteractionRange, FluidCollisionMode.NEVER, true, 0, entity -> entity instanceof LivingEntity && !entity.equals(player));

        // No entity/block in line of sight = event always called
        if (result == null) return true;

        // Block in line of sight but no entity = an event is called IIF
        // block is within block interaction range (always the case in <=1.20.5)
        Entity entity = result.getHitEntity();
        if (entity == null || entity instanceof Player && ((Player) entity).getGameMode() == GameMode.SPECTATOR) {
            double blockInteractionRangeSquared;
            if (legacyInteractionRange) blockInteractionRangeSquared = LEGACY_MAX_RANGE * LEGACY_MAX_RANGE;
            else {
                var blockInteractionRange = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();
                blockInteractionRangeSquared = blockInteractionRange * blockInteractionRange;
            }
            double distanceSquared = result.getHitPosition().distanceSquared(player.getEyeLocation().toVector());
            return distanceSquared <= blockInteractionRangeSquared;
        }

        // Entity in line of sight = event is never called
        return false;
    }

    private void triggerEvent(@NotNull Player player) {
        Bukkit.getPluginManager().callEvent(new FixPlayerInteractEvent(player));
    }
}
