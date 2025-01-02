package io.lumine.mythic.lib.manager;

import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.player.particle.ParticleEffect;
import io.lumine.mythic.lib.player.potion.PermanentPotionEffect;
import io.lumine.mythic.lib.player.skill.PassiveSkill;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @deprecated Not used
 */
@Deprecated
public class ModifierManager {
    private final Map<String, Function<ConfigObject, PlayerModifier>> modifierTypes = new HashMap<>();

    public ModifierManager() {
        registerModifierType("stat", obj -> new StatModifier(obj));
        registerModifierType("particle", ParticleEffect::fromConfig);
        registerModifierType("potion", obj -> new PermanentPotionEffect(obj));
        registerModifierType("skill", obj -> new PassiveSkill(obj));
    }

    public PlayerModifier loadPlayerModifier(ConfigObject config) {
        String configKey = config.getString("type");

        for (Map.Entry<String, Function<ConfigObject, PlayerModifier>> entry : modifierTypes.entrySet())
            if (configKey.equals(entry.getKey()))
                return entry.getValue().apply(config);

        throw new IllegalArgumentException("Could not match player modifier type to '" + configKey + "'");
    }

    public void registerModifierType(String identifier, Function<ConfigObject, PlayerModifier> configLoader) {
        Validate.notNull(identifier, "Identifier cannot be null");
        Validate.notNull(configLoader, "Function cannot be null");

        modifierTypes.put(identifier, configLoader);
    }
}
