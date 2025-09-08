package io.lumine.mythic.lib.profile;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.event.ProfileCreateEvent;
import fr.phoenixdevt.profiles.event.ProfileRemoveEvent;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.module.MMOPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @NotNull
    @Override
    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    @EventHandler
    public void onProfileCreate(ProfileCreateEvent event) {
        event.validate(this);
    }

    @EventHandler
    public void onProfileDelete(ProfileRemoveEvent event) {
        event.validate(this);
    }
}
