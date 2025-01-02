package io.lumine.mythic.lib.util.network;

import io.lumine.mythic.lib.api.event.FixPlayerInteractEvent;
import io.lumine.mythic.lib.version.Attributes;
import io.lumine.mythic.lib.version.ServerVersion;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This fixes an issue with Vanilla Minecraft which has been addressed a few
 * times already by item plugins. Some sources:
 * Sources:
 * - <a href="https://www.spigotmc.org/threads/1-19-playerinteractevent-not-called-when-entity-is-in-sight-client-bug.574671/">...</a>
 * - <a href="https://github.com/PluginBugs/Issues-ItemsAdder/issues/1993">...</a>
 * <p>
 * Minecraft <=1.20.5 Left-Click Vanilla Behaviour:
 * - distance < 3, successful melee hit, no interact packet but attack packet.
 * - distance 3 - 5, missed melee attack, no interact packet, no attack packet.
 * - distance > 5, not an attack, interact packet.
 * - at any distance, swing packet.
 * <p>
 * Minecraft post-1.20.5 Left-Click Vanilla Behaviour:
 * - distance <= entity_interaction_range, successful melee hit, no interact packet, attack packet
 * - distance > entity_interaction_range, failed melee hit, interact packet
 * <p>
 * TODO im
 * <p>
 * This class fixes when the entity is at distance <5 to send an interact
 * packet at this situation. The recreated behaviour is that interact events
 * are called EVERYTIME, even during successful hits.
 * <p>
 * Technically, only the part between 3 and 5 blocks in <=1.20.5 is problematic,
 * as PlayerInteractEvent's might be designed to only trigger when out-of-range.
 * This means that this class sort-of modifies the behaviour of PlayerInteractEvents;
 * which could lead to issues with other plugins. In hope to fix other parts
 * of the MMO plugins which might rely on left clicks triggering in canonical/logical
 * fashion, the behaviour of PlayerInteractEvent's is willingly slightly modified
 * across the entire server.
 *
 * <p>
 * In 1.20.5+, this problem is less severe, an alternative to listening to packets
 * would be to trigger skills with the LEFT_CLICK trigger during melee attacks,
 * which can easily be done using the PlayerAttackEvent from MythicLib. This will
 * be done in the future in 1.20.5+ if issues are found with this fix.
 *
 * @author Roch Blondiaux (Kiwix). 18/05/2023. Jules (08/12/2024)
 */
public class MythicPacketSniffer extends LightInjector {
    private final boolean legacy;
    private final String expectedPacketName, expectedFieldName;

    /**
     * Max range used for <=1.20.5 before 'Entity Interaction Range' was implemented
     */
    private static final double LEGACY_MAX_RANGE = 5;

    private static final Map<String, Map<String, Field>> FIELDS = new ConcurrentHashMap<>();

    /**
     * Initializes the injector and starts to listen to packets.
     * <p>
     * Note that, while it is possible to create more than one instance per plugin,
     * it's more efficient and recommended to just have only one.
     *
     * @param plugin The {@link Plugin} which is instantiating this injector.
     * @throws NullPointerException     If the provided {@code plugin} is {@code null}.
     * @throws IllegalStateException    When <b>not</b> called from the main thread.
     * @throws IllegalArgumentException If the provided {@code plugin} is not enabled.
     */
    public MythicPacketSniffer(@NotNull Plugin plugin, ServerVersion version) {
        super(plugin);

        legacy = version.isUnder(1, 20, 5);
        expectedPacketName = legacy ? "PacketPlayInArmAnimation" : "ServerboundSwingPacket";
        expectedFieldName = legacy ? "a" : "hand";
    }

    @Override
    protected @Nullable Object onPacketSendAsync(@Nullable Player receiver, @NotNull Channel channel, @NotNull Object packet) {
        return packet;
    }

    @Override
    protected @Nullable Object onPacketReceiveAsync(@Nullable Player sender, @NotNull Channel channel, @NotNull Object packet) {
        if (sender == null) return packet;
        final String packetName = packet.getClass().getSimpleName();

        if (packetName.equals(this.expectedPacketName)) {
            Object hand = getField(packet, expectedFieldName);
            if (hand == null || !hand.toString().equals("MAIN_HAND")) return packet;

            // Both the event and Entity#getNearbyEntities() need to be ran sync
            Bukkit.getScheduler().runTask(getPlugin(), () -> {
                if (!eventCalled(sender)) triggerEvent(sender);
            });
        }

        return packet;
    }

    /**
     * Approximate re-implementation of the following logic:
     * - raycast from the player towards their eye location
     * - is there an interact event being called
     *
     * @return If an event is expected to be called
     */
    private boolean eventCalled(Player player) {
        double entityInteractionRange = legacy ? LEGACY_MAX_RANGE : player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).getValue();

        RayTraceResult result = player.getWorld().rayTrace(player.getEyeLocation(), player.getEyeLocation().getDirection(), entityInteractionRange, FluidCollisionMode.NEVER, true, 0, entity -> entity instanceof LivingEntity && !entity.equals(player));

        // No entity/block in line of sight = event always called
        if (result == null) return true;

        // Block in line of sight but no entity = an event is called IIF
        // block is within block interaction range (always the case in <=1.20.5)
        Entity entity = result.getHitEntity();
        if (entity == null || entity instanceof Player && ((Player) entity).getGameMode().ordinal() == 3) {
            double blockInteractionRange = legacy ? LEGACY_MAX_RANGE : player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();
            double distance = result.getHitPosition().distance(player.getEyeLocation().toVector());
            return distance <= blockInteractionRange;
        }

        // Entity in line of sight = event is never called
        return false;
    }

    private void triggerEvent(@NotNull Player player) {
        Bukkit.getPluginManager().callEvent(new FixPlayerInteractEvent(player));
    }

    @Nullable
    private Object getField(Object object, String field) {
        return getField(object, object.getClass(), field);
    }

    @Nullable
    private Object getField(Object object, Class<?> c, String field) {
        if (FIELDS.containsKey(c.getCanonicalName())) {
            Map<String, Field> fs = FIELDS.get(c.getCanonicalName());
            if (fs.containsKey(field)) {
                try {
                    return fs.get(field).get(object);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(String.format("Could not get field "), e);
                }
            }
        }

        Class<?> current = c;
        Field f;
        while (true) try {
            f = current.getDeclaredField(field);
            break;
        } catch (ReflectiveOperationException e1) {
            current = current.getSuperclass();
            if (current != null) {
                continue;
            }
            return null;
        }

        f.setAccessible(true);

        Map<String, Field> map;
        if (FIELDS.containsKey(c.getCanonicalName())) {
            map = FIELDS.get(c.getCanonicalName());
        } else {
            map = new ConcurrentHashMap<>();
            FIELDS.put(c.getCanonicalName(), map);
        }

        map.put(f.getName(), f);

        try {
            return f.get(object);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
