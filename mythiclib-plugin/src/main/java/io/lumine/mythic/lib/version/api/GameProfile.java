package io.lumine.mythic.lib.version.api;

import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface GameProfile {

    @Nullable
    public String getTextureValue();

    @Nullable
    public UUID getUniqueId();

    @Nullable
    public String getName();

    public static GameProfile of(UUID uniqueId, String textureValue) {
        return VersionWrapper.get().newProfile(uniqueId, textureValue);
    }
}
