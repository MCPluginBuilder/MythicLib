package io.lumine.mythic.lib.profile.listener;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.ProfileProvider;
import fr.phoenixdevt.profiles.event.ProfileSelectEvent;
import fr.phoenixdevt.profiles.event.ProfileUnloadEvent;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import io.lumine.mythic.lib.data.SaveReason;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.util.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class LegacyProfileListener implements Listener {

    /**
     * This is used to hook plugins which support legacy profiles:
     * - MMOCore
     * - MMOItems
     * - MMOInventory
     * <p>
     * This class tells these plugins to load data when a profile is being
     * selected by a player. By default, player data loads on login so this
     * has to be changed to support profiles.
     */
    public static <H extends SynchronizedDataHolder> void hook(@NotNull ProfileProvider profilePlugin, @NotNull ProfileDataModule module, @NotNull SynchronizedDataManager<H, ?> manager, @NotNull Listener fictiveListener, @NotNull EventPriority joinEventPriority, @NotNull EventPriority quitEventPriority) {

        // Register data holder
        profilePlugin.registerModule(module);

        // Load data on profile select
        UtilityMethods.registerEvent(ProfileSelectEvent.class, fictiveListener, joinEventPriority, event -> {
            final @NotNull H data = manager.get(event.getPlayer());
            manager.loadData(data).thenAccept(Tasks.sync(manager.getOwningPlugin(), v -> {
                Bukkit.getPluginManager().callEvent(new SynchronizedDataLoadEvent(manager, data, event));
            }));
        }, manager.getOwningPlugin(), false);

        // TODO remove data from other plugins when removing profiles in order to empty databases

        // Save data on profile unload
        UtilityMethods.registerEvent(ProfileUnloadEvent.class, fictiveListener, quitEventPriority, event -> {
            manager.unregister(event.getPlayer(), adapt(event.getReason()));
        }, manager.getOwningPlugin(), false);
    }

    @NotNull
    private static SaveReason adapt(ProfileUnloadEvent.Reason reason) {
        switch (reason) {
            case PLAYER:
            case COMMAND:
                return SaveReason.QUIT_PROFILE;
            case LOG_OUT:
                return SaveReason.LOG_OUT;
            default:
                throw new IllegalArgumentException("Cannot adapt reason " + reason);
        }
    }
}
