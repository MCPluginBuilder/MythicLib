package io.lumine.mythic.lib.player.particle.type;

import io.lumine.mythic.lib.player.particle.ParticleEffect;
import io.lumine.mythic.lib.player.particle.ParticleEffectType;
import io.lumine.mythic.lib.player.particle.ParticleInformation;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;

import java.util.Map;

public class DoubleRingsParticleEffect extends ParticleEffect {
    private final double speed, height, radius, r_speed, y_offset;

    private double a = 0;

    public DoubleRingsParticleEffect(String key, Map<String, Double> modifiers, ParticleInformation information) {
        super(key, information);

        speed = resolveModifier(modifiers, "speed");
        height = resolveModifier(modifiers, "height");
        radius = resolveModifier(modifiers, "radius");
        r_speed = resolveModifier(modifiers, "rotation-speed");
        y_offset = resolveModifier(modifiers, "y-offset");
    }

    public DoubleRingsParticleEffect(ConfigObject obj) {
        super(obj);

        speed = obj.getDouble("speed");
        height = obj.getDouble("height");
        radius = obj.getDouble("radius");
        r_speed = obj.getDouble("rotation-speed");
        y_offset = obj.getDouble("y-offset");
    }

    @Override
    public ParticleEffectType getType() {
        return ParticleEffectType.DOUBLE_RINGS;
    }

    @Override
    public void tick() {
        Location loc = player.getLocation();
        for (double k = 0; k < 2; k++) {
            double a = this.a + k * Math.PI;
            particle.display(loc.clone().add(radius * Math.cos(a), height + Math.sin(this.a) * y_offset, radius * Math.sin(a)), speed);
        }

        a += Math.PI / 16 * r_speed;
        if (a > Math.PI * 2) a -= Math.PI * 2;
    }
}
