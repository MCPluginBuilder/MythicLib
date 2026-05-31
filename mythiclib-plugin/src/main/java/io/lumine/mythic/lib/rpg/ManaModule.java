package io.lumine.mythic.lib.rpg;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import org.jetbrains.annotations.NotNull;

/**
 * Most RPG plugins only have two resources, mana and stamina. Some RPG
 * plugins do not refer to them as "mana" and "stamina". Mana should always
 * refer to the main resource type, and Stamina to the second one, if it exists.
 */
public interface ManaModule {

    public double getMana(@NotNull MMOPlayerData playerData);

    public double getStamina(@NotNull MMOPlayerData playerData);

    public boolean setMana(@NotNull MMOPlayerData playerData, double newValue, @NotNull ResourceUpdateReason reason);

    public boolean setStamina(@NotNull MMOPlayerData playerData, double newValue, @NotNull ResourceUpdateReason reason);

    @NotNull
    public static ManaModule from(@NotNull String pluginName) {
        try {
            var hook = UtilityMethods.prettyValueOf(RPGPluginEnum::valueOf, pluginName, "No mana plugin %s");
            var newInstance = hook.instantiateHook();
            if (!(newInstance instanceof ManaModule)) throw new IllegalArgumentException("Plugin " + pluginName + " does not support mana");
            return (ManaModule) newInstance;
        } catch (LinkageError | Exception exception) {
            throw new IllegalArgumentException("Could not load mana plugin " + pluginName + ", using default", exception);
        }
    }
}
