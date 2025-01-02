package io.lumine.mythic.lib.player.particle;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.Closeable;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.player.particle.type.*;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @deprecated Not implemented yet
 */
@Deprecated
public abstract class ParticleEffect extends PlayerModifier implements Closeable {
    private final ParticleInformation particleUsed;

    private BukkitRunnable particleRunnable;

    public ParticleEffect(ConfigObject obj) {
        super(obj.getString("key"), EquipmentSlot.OTHER, ModifierSource.OTHER);

        particleUsed = new ParticleInformation(obj.getObject("particle"));
    }

    public int getTick() {
        return 1;
    }

    public ParticleInformation getParticle() {
        return particleUsed;
    }

    /**
     * What the particle effect actually does
     */
    public abstract void tick(Player player);

    @Override
    public void register(MMOPlayerData playerData) {
        playerData.getParticleEffectMap().addModifier(this);
    }

    @Override
    public void unregister(MMOPlayerData playerData) {
        playerData.getParticleEffectMap().removeModifier(getUniqueId());
    }

    @Override
    public void close() {
        particleRunnable.cancel();
    }

    private static final Map<String, Function<ConfigObject, ParticleEffect>> BY_NAME = new HashMap<>();

    static {
        registerParticleEffectType("AURA", obj -> new AuraParticleEffect(obj));
        registerParticleEffectType("DOUBLE_RINGS", obj -> new DoubleRingsParticleEffect(obj));
        registerParticleEffectType("FIREFLIES", obj -> new FirefliesParticleEffect(obj));
        registerParticleEffectType("GALAXY", obj -> new GalaxyParticleEffect(obj));
        registerParticleEffectType("HELIX", obj -> new HelixParticleEffect(obj));
        registerParticleEffectType("OFFSET", obj -> new OffsetParticleEffect(obj));
        registerParticleEffectType("VORTEX", obj -> new VortexParticleEffect(obj));
        registerParticleEffectType("AURA", obj -> new AuraParticleEffect(obj));
    }

    public static ParticleEffect fromConfig(ConfigObject obj) {
        Function<ConfigObject, ParticleEffect> configReader = BY_NAME.get(obj.getString("particle-effect"));
        Validate.notNull(configReader, "Could not find particle effect type with ID '" + obj.getString("particle-effect") + "'");
        return configReader.apply(obj);
    }

    public static void registerParticleEffectType(String id, Function<ConfigObject, ParticleEffect> configReader) {
        Validate.notNull(id, "Identifier cannot be null");
        Validate.notNull(configReader, "Function cannot be null");

        BY_NAME.put(id, configReader);
    }
}
