package io.lumine.mythic.lib.profile.handler;

import fr.phoenixdevt.profiles.ProfileDataModule;
import fr.phoenixdevt.profiles.ProfileProvider;
import fr.phoenixdevt.profiles.event.ProfileUnloadEvent;
import io.lumine.mythic.lib.data.SaveReason;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LegacyProfileHandler implements ProfileHandler {
    private final ProfileProvider profileProvider;

    public LegacyProfileHandler() {
        this.profileProvider = Bukkit.getServicesManager().getRegistration(ProfileProvider.class).getProvider();
        Validate.notNull(profileProvider, "Could not find ProfileAPI service provider");
    }

    @Override
    public List<NamespacedKey> collectModules() {

        Bukkit.broadcastMessage("collecting modules legacy profile handler " + this.profileProvider.getModules().stream().map(ProfileDataModule::getId).collect(Collectors.toList()));

        // Collect modules at runtime to avoid on-startup timing issues
        return new ArrayList<>(this.profileProvider.getModules().stream().map(ProfileDataModule::getId).collect(Collectors.toList()));
    }
}
