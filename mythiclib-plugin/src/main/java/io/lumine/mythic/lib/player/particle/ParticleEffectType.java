package io.lumine.mythic.lib.player.particle;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.player.particle.type.*;
import io.lumine.mythic.lib.util.TriFunction;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class ParticleEffectType {
    private final Function<ConfigObject, ParticleEffect> parser;
    private final TriFunction<String, Map<String, Double>, ParticleInformation, ParticleEffect> instantiator;
    private final String id, name, description;
    private final int period;
    private final boolean priority;
    private final Map<String, Double> modifiers;

    public ParticleEffectType(String id,
                              Function<ConfigObject, ParticleEffect> configParser,
                              TriFunction<String, Map<String, Double>, ParticleInformation, ParticleEffect> instantiator,
                              int period, boolean priority, String description, Map<String, Double> modifiers) {
        this.id = id;
        this.parser = configParser;
        this.instantiator = instantiator;
        this.name = UtilityMethods.caseOnWords(id.toLowerCase().replace("_", " "));
        this.period = period;
        this.priority = priority;
        this.description = description;
        this.modifiers = modifiers;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    public int getPeriod() {
        return period;
    }

    public boolean hasPriority() {
        return priority;
    }

    @NotNull
    public Function<ConfigObject, ParticleEffect> getParser() {
        return parser;
    }

    @NotNull
    public TriFunction<String, Map<String, Double>, ParticleInformation, ParticleEffect> getInstantiator() {
        return instantiator;
    }

    @NotNull
    public Set<String> getModifiers() {
        return modifiers.keySet();
    }

    public double getDefaultModifierValue(String modifier) {
        return Objects.requireNonNull(modifiers.get(modifier), String.format("Modifier '%s' not found", modifier));
    }

    //region Static

    private static final Map<String, ParticleEffectType> BY_NAME = new HashMap<>();

    public static final ParticleEffectType AURA = new ParticleEffectType("AURA", AuraParticleEffect::new, AuraParticleEffect::new, 1, true, "Particles flying around you.", Map.of("amount", 3d, "speed", 0d, "rotation-speed", 1d, "y-speed", 1d, "y-offset", .7, "radius", 1.3, "height", 1d)),
            DOUBLE_RINGS = new ParticleEffectType("DOUBLE_RINGS", DoubleRingsParticleEffect::new, DoubleRingsParticleEffect::new, 1, true, "Particles drawing two rings around you.", Map.of("radius", .8d, "y-offset", .4d, "height", 1d, "speed", 0d, "rotation-speed", 1d)),
            FIREFLIES = new ParticleEffectType("FIREFLIES", FirefliesParticleEffect::new, FirefliesParticleEffect::new, 1, true, "A horizontal swirl of particles around you.", Map.of("amount", 3d, "speed", 0d, "rotation-speed", 1d, "radius", 1.3, "height", 1d)),
            GALAXY = new ParticleEffectType("GALAXY", GalaxyParticleEffect::new, GalaxyParticleEffect::new, 1, true, "Particles flying outwards; looks like a galaxy.", Map.of("height", 1d, "speed", 1d, "y-coord", 0d, "rotation-speed", 1d, "amount", 6d)),
            HELIX = new ParticleEffectType("HELIX", HelixParticleEffect::new, HelixParticleEffect::new, 1, true, "Particles flying around you, forming a sphere.", Map.of("radius", .8, "height", .6, "rotation-speed", 1d, "y-speed", 1d, "amount", 4d, "speed", 0d)),
            OFFSET = new ParticleEffectType("OFFSET", OffsetParticleEffect::new, OffsetParticleEffect::new, 5, false, "Some particles randomly spawning around your body.", Map.of("amount", 5d, "vertical-offset", .5, "horizontal-offset", .3, "speed", 0d, "height", 1d)),
            VORTEX = new ParticleEffectType("VORTEX", VortexParticleEffect::new, VortexParticleEffect::new, 1, true, "Particles flying around you in a cone shape.", Map.of("radius", 1.5, "height", 2.4, "speed", 0d, "y-speed", 1d, "rotation-speed", 1d, "amount", 3d));

    static {
        register(AURA);
        register(DOUBLE_RINGS);
        register(FIREFLIES);
        register(GALAXY);
        register(HELIX);
        register(OFFSET);
        register(VORTEX);
    }

    public static boolean isValid(@NotNull String id) {
        return BY_NAME.containsKey(id);
    }

    @NotNull
    public static Collection<ParticleEffectType> getAll() {
        return BY_NAME.values();
    }

    @NotNull
    public static ParticleEffectType get(@NotNull String id) {
        return Objects.requireNonNull(BY_NAME.get(id), String.format("Could not find particle effect type with ID %s", id));
    }

    public static void register(@NotNull ParticleEffectType particleEffectType) {
        Validate.notNull(particleEffectType, "Particle effect type cannot be null");
        Validate.isTrue(!BY_NAME.containsKey(particleEffectType.getId()), "A particle effect type with the same ID already exists");

        BY_NAME.put(particleEffectType.getId(), particleEffectType);
    }

    //endregion
}
