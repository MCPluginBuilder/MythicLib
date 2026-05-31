package io.lumine.mythic.lib.rpg;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.provided.PlaceholderLevelModule;
import org.jetbrains.annotations.NotNull;

public interface LevelModule {

    public int getLevel(@NotNull MMOPlayerData player);

    @NotNull
    public static LevelModule from(@NotNull String pluginName) {

        if (pluginName.contains("%")) return new PlaceholderLevelModule(pluginName);

        try {
            var hook = UtilityMethods.prettyValueOf(RPGPluginEnum::valueOf, pluginName, "No level plugin %s");
            var newInstance = hook.instantiateHook();
            if (!(newInstance instanceof LevelModule)) throw new IllegalArgumentException("Plugin " + pluginName + " does not support levels");
            return (LevelModule) newInstance;
        } catch (LinkageError | Exception exception) {
            throw new IllegalArgumentException("Could not load level plugin " + pluginName + ", using default", exception);
        }
    }
}
