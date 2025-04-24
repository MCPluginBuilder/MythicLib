package io.lumine.mythic.lib.player.particle.type;

import io.lumine.mythic.lib.player.particle.ParticleEffect;
import io.lumine.mythic.lib.player.particle.ParticleEffectType;
import io.lumine.mythic.lib.player.particle.ParticleInformation;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;

import java.util.Map;

public class AuraParticleEffect extends ParticleEffect {
    private final double speed, height, radius, r_speed, y_offset, y_speed;
    private final int amount;

    private double a = 0;

    public AuraParticleEffect(String key, Map<String, Double> modifiers, ParticleInformation information) {
        super(key, information);

        speed = resolveModifier(modifiers, "speed");
        height = resolveModifier(modifiers, "height");
        radius = resolveModifier(modifiers, "radius");
        r_speed = resolveModifier(modifiers, "rotation-speed");
        y_speed = resolveModifier(modifiers, "y-speed");
        y_offset = resolveModifier(modifiers, "y-offset");
        amount = (int) resolveModifier(modifiers, "amount");
    }

    public AuraParticleEffect(ConfigObject obj) {
        super(obj);

        speed = obj.getDouble("speed");
        height = obj.getDouble("height");
        radius = obj.getDouble("radius");
        r_speed = obj.getDouble("rotation-speed");
        y_speed = obj.getDouble("y-speed");
        y_offset = obj.getDouble("y-offset");
        amount = obj.getInt("amount");
    }

    @Override
    public ParticleEffectType getType() {
        return ParticleEffectType.AURA;
    }

    @Override
    public void tick() {
        Location loc = player.getLocation();
        for (int k = 0; k < amount; k++) {
            double a = this.a + Math.PI * 2 * k / amount;
            particle.display(loc.clone().add(Math.cos(a) * radius, Math.sin(this.a * y_speed * 3) * y_offset + height, Math.sin(a) * radius), speed);
        }

        a += Math.PI / 48 * r_speed;
        if (a > Math.PI * 2 / y_speed) a -= Math.PI * 2 / y_speed;
    }
}
