package io.lumine.mythic.lib.version.api;

import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ModernGameProfile implements GameProfile {
    public final PlayerProfile bukkit;

    public ModernGameProfile(PlayerProfile bukkit) {
        this.bukkit = bukkit;
    }

    @Override
    public String getTextureValue() {
        var skin = bukkit.getTextures().getSkin();
        return skin == null ? null : skin.toString();
    }

    @Override
    @Nullable
    public UUID getUniqueId() {
        return bukkit.getUniqueId();
    }

    @Override
    @Nullable
    public String getName() {
        return bukkit.getName();
    }
}
