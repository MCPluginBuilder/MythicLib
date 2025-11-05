package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

public class WrapperUtils {

    public static final String PLAYER_PROFILE_NAME = "SkullTexture";

    private static final String URL_PREFIX = "\"url\":\"";
    private static final String URL_SUFFIX = "\"";

    @NotNull
    public static String extractTextureUrl(@NotNull String stringInput) {

        // If URL, return
        if (stringInput.startsWith("http")) return stringInput;

        int start = stringInput.indexOf(URL_PREFIX);
        Validate.isTrue(start >= 0, "Could not find URL prefix in decoded skull value");
        start += URL_PREFIX.length();
        final var end = stringInput.indexOf(URL_SUFFIX, start);
        return stringInput.substring(start, end);
    }
}
