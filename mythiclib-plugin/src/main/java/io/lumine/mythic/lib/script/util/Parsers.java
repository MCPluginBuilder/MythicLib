package io.lumine.mythic.lib.script.util;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.function.Function;

public class Parsers {

    public static final Function<String, PotionEffectType> POTION_EFFECT_TYPE =
            input -> UtilityMethods.prettyValueOf(PotionEffectType::getByName, input, "No potion effect with ID %s");

    public static final Function<String, DamageType> DAMAGE_TYPE =
            input -> UtilityMethods.prettyValueOf(DamageType::valueOf, input, "No damage type with ID %s");

    public static final Function<String, List<DamageType>> DAMAGE_TYPES = DamageType::listFromConfig;

}
