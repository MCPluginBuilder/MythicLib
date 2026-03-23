package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.UtilityMethods;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class Biomes {

    @NotNull
    public static Biome fromName(String... candidates) {
        return UtilityMethods.resolveField(getResolver(), candidates);
    }

    private static Function<String, Biome> RESOLVER;

    private static Function<String, Biome> getResolver() {
        if (RESOLVER == null)
            try {
                var method = Biome.class.getDeclaredMethod("valueOf", String.class);
                RESOLVER = str -> {
                    try {
                        return (Biome) method.invoke(null, str);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                };
            } catch (Exception exception) {
                throw new RuntimeException("Reflection error: " + exception.getMessage());
            }

        return RESOLVER;
    }
}
