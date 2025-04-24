package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

import java.util.function.Function;

@Deprecated
public class SkillModifierManager {

    @Deprecated
    public void registerModifierType(String key, Function<ConfigObject, PlayerModifier> resolver, String... aliases) {
        PlayerModifier.registerPlayerModifierType(key, resolver, aliases);
    }
}
