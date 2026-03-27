package io.lumine.mythic.lib.script.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason;
import io.lumine.mythic.lib.script.mechanic.shaped.RayTraceMechanic;
import io.lumine.mythic.lib.script.variable.VariableScope;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.EntityLocationType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class Parsers {

    public static final Function<String, PotionEffectType> POTION_EFFECT_TYPE =
            input -> UtilityMethods.prettyValueOf(PotionEffectType::getByName, input, "No potion effect with ID '%s'");

    public static final Function<String, DamageType> DAMAGE_TYPE =
            input -> UtilityMethods.prettyValueOf(DamageType::valueOf, input, "No damage type with ID '%s'");

    public static final Function<String, TriggerType> SKILL_TRIGGER =
            input -> UtilityMethods.prettyValueOf(TriggerType::valueOf, input, "No trigger with ID '%s'");

    public static final Function<String, List<DamageType>> DAMAGE_TYPES = DamageType::listFromConfig;

    public static final Function<String, RayTraceMechanic.RayTraceType> RAY_TRACE_TYPE = ofEnum(RayTraceMechanic.RayTraceType.class, RayTraceMechanic.RayTraceType::valueOf);

    public static final Function<String, Material> MATERIAL = ofEnum(Material.class, Material::valueOf);

    public static final Function<String, Particle> PARTICLE = ofEnum(Particle.class, Particle::valueOf);

    public static final Function<String, VariableScope> VARIABLE_SCOPE = ofEnum(VariableScope.class, VariableScope::valueOf);

    public static final Function<String, SkillHandler<?>> SKILL_HANDLER =
            input -> MythicLib.plugin.getSkills().getHandlerOrThrow(UtilityMethods.enumName(input));

    public static final Function<String, InteractionType> INTERACTION_TYPE = Parsers.ofEnum(InteractionType.class, InteractionType::valueOf);

    public static final Function<String, EntityLocationType> ENTITY_LOCATION_TYPE = Parsers.ofEnum(EntityLocationType.class, EntityLocationType::valueOf);

    public static final Function<String, ResourceUpdateReason> RESOURCE_UPDATE_REASON = Parsers.ofEnum(ResourceUpdateReason.class, ResourceUpdateReason::valueOf);

    @NotNull
    public static <T> Function<String, T> ofEnum(Class<T> enumClass, Function<String, T> valueOf) {
        var errorMessage = "No " + enumClass.getSimpleName() + " with ID '%s'";
        return input -> UtilityMethods.prettyValueOf(valueOf, input, errorMessage);
    }
}
