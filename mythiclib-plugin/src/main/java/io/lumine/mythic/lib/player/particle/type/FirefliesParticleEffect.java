package io.lumine.mythic.lib.player.particle.type;

import io.lumine.mythic.lib.player.particle.ParticleEffect;
import io.lumine.mythic.lib.player.particle.ParticleEffectType;
import io.lumine.mythic.lib.player.particle.ParticleInformation;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;

import java.util.Map;

public class FirefliesParticleEffect extends ParticleEffect {
    private final double speed, height, radius, r_speed;
    private final int amount;

    private double a = 0;

    public FirefliesParticleEffect(String key, Map<String, Double> modifiers, ParticleInformation information) {
        super(key, information);

        speed = resolveModifier(modifiers, "speed");
        height = resolveModifier(modifiers, "height");
        radius = resolveModifier(modifiers, "radius");
        r_speed = resolveModifier(modifiers, "rotation-speed");
        amount = (int) resolveModifier(modifiers, "amount");
    }

    public FirefliesParticleEffect(ConfigObject obj) {
        super(obj);

        speed = obj.getDouble("speed");
        height = obj.getDouble("height");
        radius = obj.getDouble("radius");
        r_speed = obj.getDouble("rotation-speed");
        amount = obj.getInt("amount");
    }

    @Override
    public ParticleEffectType getType() {
        return ParticleEffectType.FIREFLIES;
    }

    @Override
    public void tick() {
        Location loc = player.getLocation();
        for (int k = 0; k < amount; k++) {
            double a = this.a + Math.PI * 2 * k / amount;
            particle.display(loc.clone().add(Math.cos(a) * radius, height, Math.sin(a) * radius), speed);
        }

        a += Math.PI / 48 * r_speed;
        if (a > Math.PI * 2) a -= Math.PI * 2;
    }
}
