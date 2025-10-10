package io.lumine.mythic.lib.profile;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.event.ProfileCreateEvent;
import fr.phoenixdevt.profiles.event.ProfileRemoveEvent;
import fr.phoenixdevt.profiles.event.ProfileSelectEvent;
import fr.phoenixdevt.profiles.event.ProfileUnloadEvent;
import io.lumine.mythic.lib.data.SaveReason;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.module.MMOPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Used for simple data plugins like MMOItems or MMOInventory,
 * featuring profile data support but which do not have any
 * placeholders or profile-based features.
 * <p>
 * Very basic implementation of event listening for {@link ProfileCreateEvent}
 * and {@link ProfileRemoveEvent}, the two other profile events being already
 * implemented inside of {@link SynchronizedDataManager#initialize(EventPriority, EventPriority)}
 *
 * @author Jules
 */
public class DefaultProfileDataModule implements ProfileDataModule {
    private final MMOPlugin plugin;
    private final NamespacedKey namespacedKey;

    public DefaultProfileDataModule(@NotNull MMOPlugin plugin) {
        this.plugin = plugin;
        this.namespacedKey = plugin.getNamespacedKey();
    }

    @NotNull
    @Override
    public MMOPlugin getOwningPlugin() {
        return plugin;
    }

    @Override
    public @NotNull NamespacedKey getId() {
        return namespacedKey;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @EventHandler
    public void onProfileSelect(ProfileSelectEvent event) {
        final SynchronizedDataManager manager = plugin.getRawPlayerDataManager();
        final var playerData = manager.get(event.getPlayer());

        manager.loadData(playerData, event);
    }

    @EventHandler
    public void onProfileUnload(ProfileUnloadEvent event) {
        plugin.getRawPlayerDataManager().unregister(event.getPlayer(), adapt(event.getReason()));
    }

    @EventHandler
    public void onProfileCreate(ProfileCreateEvent event) {
        // Nothing is needed really
        event.validate(this);
    }

    @EventHandler
    public void onProfileDelete(ProfileRemoveEvent event) {
        // TODO empty database
        event.validate(this);
    }

    /*
    private boolean isLegacyProfileMode() {
        return MythicLib.plugin.getProfileMode() == ProfileMode.LEGACY;
    }
    */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogout(PlayerQuitEvent event) {
        // garbage collect player data
        plugin.getRawPlayerDataManager().garbageCollect(event.getPlayer());
    }

    @NotNull
    private static SaveReason adapt(ProfileUnloadEvent.Reason reason) {
        switch (reason) {
            case QUIT_PROFILE:
            case SWITCH_PROFILE:
                return SaveReason.QUIT_PROFILE;
            case LOG_OUT:
                return SaveReason.LOG_OUT;
            default:
                throw new IllegalArgumentException("Cannot adapt reason " + reason);
        }
    }
}
