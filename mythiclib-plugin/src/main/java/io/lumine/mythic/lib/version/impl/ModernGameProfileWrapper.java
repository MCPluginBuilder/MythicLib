package io.lumine.mythic.lib.version.impl;

import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.api.GameProfile;
import io.lumine.mythic.lib.version.api.ModernGameProfile;
import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

/**
 * Wrapper for GameProfile handling in 1.20+
 */
public interface ModernGameProfileWrapper extends VersionWrapper {

    @Override
    public default GameProfile getProfile(SkullMeta meta) {
        var found = meta.getOwnerProfile();
        return found == null ? null : new ModernGameProfile(found);
    }

    @Override
    public default void setProfile(SkullMeta meta, GameProfile profile) {
        if (profile == null) meta.setOwnerProfile(null);
        else meta.setOwnerProfile(((ModernGameProfile) profile).bukkit);
    }

    @Override
    public default ModernGameProfile newProfile(UUID uniqueId, String textureValue) {
        final var profile = Bukkit.getServer().createPlayerProfile(uniqueId, PLAYER_PROFILE_NAME);
        final var stringUrl = extractUrl(new String(Base64.getDecoder().decode(textureValue)));
        final URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Could not create new player profile", exception);
        }
        profile.getTextures().setSkin(url);
        return new ModernGameProfile(profile);
    }

    static final String URL_PREFIX = "\"url\":\"";
    static final String URL_SUFFIX = "\"";

    private String extractUrl(String str) {
        int start = str.indexOf(URL_PREFIX);
        Validate.isTrue(start >= 0, "Could not find prefix in decoded skull value");
        start += URL_PREFIX.length();
        final int end = str.indexOf(URL_SUFFIX, start);
        return str.substring(start, end);
    }
}
