package io.lumine.mythic.lib.profile.handler;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.ProfileProvider;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LegacyProfileHandler implements ProfileHandler {
    private final Lazy<ProfileProvider> profileProvider;

    public LegacyProfileHandler() {
        this.profileProvider = Lazy.of(() -> Bukkit.getServicesManager().getRegistration(ProfileProvider.class).getProvider());
        Validate.notNull(profileProvider, "Could not find ProfileAPI service provider");
    }

    @Override
    public void onStartup() {
        // No need
    }

    @Override
    public List<NamespacedKey> collectModules() {
        // Collect modules at runtime to avoid on-startup timing issues
        return new ArrayList<>(this.profileProvider.get().getModules().stream().map(ProfileDataModule::getId).collect(Collectors.toList()));
    }
}
