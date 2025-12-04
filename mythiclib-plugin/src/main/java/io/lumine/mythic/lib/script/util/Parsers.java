package io.lumine.mythic.lib.script.util;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.script.condition.generic.CompareCondition;
import io.lumine.mythic.lib.script.mechanic.shaped.RayTraceMechanic;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.function.Function;

public class Parsers {

    public static final Function<String, PotionEffectType> POTION_EFFECT_TYPE =
            input -> UtilityMethods.prettyValueOf(PotionEffectType::getByName, input, "No potion effect with ID %s");

    public static final Function<String, DamageType> DAMAGE_TYPE =
            input -> UtilityMethods.prettyValueOf(DamageType::valueOf, input, "No damage type with ID %s");

    public static final Function<String, List<DamageType>> DAMAGE_TYPES = DamageType::listFromConfig;

    public static final Function<String, RayTraceMechanic.RayTraceType> RAY_TRACE_TYPE =
            input -> UtilityMethods.prettyValueOf(RayTraceMechanic.RayTraceType::valueOf, input, "No ray trace type with ID %s");

    public static final Function<String, CompareCondition.Comparator> COMPARATOR =
            input -> UtilityMethods.prettyValueOf(CompareCondition.Comparator::fromString, input, "No comparator with ID %s");
}
