package io.lumine.mythic.lib.player.particle.type;

import io.lumine.mythic.lib.player.particle.ParticleEffect;
import io.lumine.mythic.lib.player.particle.ParticleEffectType;
import io.lumine.mythic.lib.player.particle.ParticleInformation;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;

import java.util.Map;

public class VortexParticleEffect extends ParticleEffect {
    private final double speed, height, radius, r_speed, y_speed;
    private final int amount;

    private double j = 0;

    public VortexParticleEffect(String key, Map<String, Double> modifiers, ParticleInformation information) {
        super(key, information);

        speed = resolveModifier(modifiers, "speed");
        height = resolveModifier(modifiers, "height");
        radius = resolveModifier(modifiers, "radius");
        y_speed = resolveModifier(modifiers, "y-speed");
        r_speed = resolveModifier(modifiers, "rotation-speed");
        amount = (int) resolveModifier(modifiers, "amount");
    }

    public VortexParticleEffect(ConfigObject obj) {
        super(obj);

        speed = obj.getDouble("speed");
        height = obj.getDouble("height");
        radius = obj.getDouble("radius");
        y_speed = obj.getDouble("y-speed");
        r_speed = obj.getDouble("rotation-speed");
        amount = obj.getInt("amount");
    }

    @Override
    public ParticleEffectType getType() {
        return ParticleEffectType.VORTEX;
    }

    @Override
    public void tick() {
        Location loc = player.getLocation();
        double r = j / Math.PI / 2;
        for (int k = 0; k < amount; k++) {
            double a = j + Math.PI * 2 * k / amount;
            particle.display(loc.clone().add(Math.cos(a) * radius * (1 - r * y_speed), r * y_speed * height, Math.sin(a) * radius * (1 - r * y_speed)), speed);
        }

        j += Math.PI / 24 * r_speed;
        if (j > Math.PI * 2 / y_speed) j -= Math.PI * 2 / y_speed;
    }
}
