package io.lumine.mythic.lib.version.api;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LegacyGameProfile implements GameProfile {
    public final com.mojang.authlib.GameProfile bukkit;

    public LegacyGameProfile(com.mojang.authlib.GameProfile bukkit) {
        this.bukkit = bukkit;
    }

    @Override
    public String getTextureValue() {
        for (var prop : bukkit.getProperties().get("textures"))
            return prop.getValue();
        return null;
    }

    @Override
    @Nullable
    public UUID getUniqueId() {
        return bukkit.getId();
    }

    @Override
    @Nullable
    public String getName() {
        return bukkit.getName();
    }
}
