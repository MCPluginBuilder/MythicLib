package io.lumine.mythic.lib.util.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Used to build a GSON kernel that supports basic Bukkit objects
 * like potion effects, locations. Other plugins can register
 * type adapters BEFORE MythicLib loads by accessing {@link #getBuilder()}.
 *
 * @author Jules
 */
public class MythicLibGson {

    @Nullable
    private static GsonBuilder builder = new GsonBuilder();

    public static GsonBuilder getBuilder() {
        return Objects.requireNonNull(builder, "MythicLib GSON already built");
    }

    public static Gson build() {
        Validate.notNull(builder, "MythicLib GSON already built");

        // Basic type adapters
        builder.registerTypeAdapter(PotionEffect.class, new PotionEffectAdapter());
        builder.registerTypeAdapter(Location.class, new LocationAdapter());
        builder.registerTypeAdapter(AttributeModifier.class, MythicLib.plugin.getVersion().isAbove(1, 21) ? new AttributeModifierAdapter() : new LegacyAttributeModifierAdapter());

        final Gson gson = builder.create();
        builder = null;
        return gson;
    }
}
