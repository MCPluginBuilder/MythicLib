package io.lumine.mythic.lib.rpg;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.provided.PlaceholderClassModule;
import org.jetbrains.annotations.NotNull;

public interface ClassModule {

    @NotNull
    public String getClass(@NotNull MMOPlayerData playerData);

    @NotNull
    public static ClassModule from(@NotNull String pluginName) {

        // Placeholder
        if (pluginName.contains("%")) return new PlaceholderClassModule(pluginName);

        try {
            var hook = UtilityMethods.prettyValueOf(RPGPluginEnum::valueOf, pluginName, "No class plugin %s");
            var newInstance = hook.instantiateHook();
            if (!(newInstance instanceof ClassModule)) throw new IllegalArgumentException("Plugin " + pluginName + " does not support classes");
            return (ClassModule) newInstance;
        } catch (LinkageError | Exception exception) {
            throw new IllegalArgumentException("Could not load class plugin " + pluginName + ", using default", exception);
        }
    }
}
