package io.lumine.mythic.lib.player.particle.type;

import io.lumine.mythic.lib.player.particle.ParticleEffect;
import io.lumine.mythic.lib.player.particle.ParticleEffectType;
import io.lumine.mythic.lib.player.particle.ParticleInformation;
import io.lumine.mythic.lib.util.configobject.ConfigObject;

import java.util.Map;

public class OffsetParticleEffect extends ParticleEffect {
    private final double speed, h_offset, v_offset, height;
    private final int amount;

    public OffsetParticleEffect(String key, Map<String, Double> modifiers, ParticleInformation information) {
        super(key, information);

        speed = resolveModifier(modifiers, "speed");
        height = resolveModifier(modifiers, "height");
        h_offset = resolveModifier(modifiers, "horizontal-offset");
        v_offset = resolveModifier(modifiers, "vertical-offset");
        amount = (int) resolveModifier(modifiers, "amount");
    }

    public OffsetParticleEffect(ConfigObject obj) {
        super(obj);

        speed = obj.getDouble("speed");
        height = obj.getDouble("height");
        h_offset = obj.getDouble("horizontal-offset");
        v_offset = obj.getDouble("vertical-offset");
        amount = obj.getInt("amount");
    }

    @Override
    public ParticleEffectType getType() {
        return ParticleEffectType.OFFSET;
    }

    @Override
    public void tick() {
        particle.display(player.getLocation().add(0, height, 0), amount, h_offset, v_offset, h_offset, speed);
    }
}
