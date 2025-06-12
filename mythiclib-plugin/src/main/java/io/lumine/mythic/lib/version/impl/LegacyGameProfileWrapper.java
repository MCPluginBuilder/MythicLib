package io.lumine.mythic.lib.version.impl;

import com.mojang.authlib.properties.Property;
import io.lumine.mythic.lib.version.api.GameProfile;
import io.lumine.mythic.lib.version.api.LegacyGameProfile;
import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public interface LegacyGameProfileWrapper extends VersionWrapper {

    @Override
    public default GameProfile getProfile(SkullMeta meta) {
        try {

            // Access field using reflection
            final Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            final var profileObject = (com.mojang.authlib.GameProfile) profileField.get(meta);
            profileField.setAccessible(false);

            return new LegacyGameProfile(profileObject);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalArgumentException("Could not fetch skull profile:" + exception.getMessage());
        }
    }

    @Override
    public default void setProfile(SkullMeta meta, GameProfile profile) {
        try {
            final Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile == null ? null : ((LegacyGameProfile) profile).bukkit);
            profileField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalArgumentException("Could not apply skull profile:" + exception.getMessage());
        }
    }

    @Override
    public default GameProfile newProfile(UUID uniqueId, String textureValue) {
        final var profile = new com.mojang.authlib.GameProfile(uniqueId, PLAYER_PROFILE_NAME);
        profile.getProperties().put("textures", new Property("textures", textureValue));
        return new LegacyGameProfile(profile);
    }

}
