package io.lumine.mythic.lib.profile.handler;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.ProfileProvider;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.stream.Collectors;

public class ProxyProfileHandler implements ProfileHandler {
    private final Lazy<ProfileProvider> profileProvider;

    public ProxyProfileHandler() {
        // Using Lazy as service might not be available at the time of ProfileHandler initialization
        this.profileProvider = Lazy.of(() -> {
            final var reg = Bukkit.getServicesManager().getRegistration(ProfileProvider.class);
            Validate.notNull(reg, "No ProfileProvider service found");
            return reg.getProvider();
        });
    }

    @Override
    public void onStartup() {
        // Nothing needed
    }

    @Override
    public List<NamespacedKey> collectModules() {
        // Collect modules at runtime to avoid on-startup timing issues
        return this.profileProvider.get().getModules().stream().map(ProfileDataModule::getId).collect(Collectors.toList());
    }
}
