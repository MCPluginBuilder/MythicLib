package io.lumine.mythic.lib.player.particle.type;

import io.lumine.mythic.lib.player.particle.ParticleEffect;
import io.lumine.mythic.lib.player.particle.ParticleEffectType;
import io.lumine.mythic.lib.player.particle.ParticleInformation;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;

import java.util.Map;

public class GalaxyParticleEffect extends ParticleEffect {
    private final double speed, height, r_speed, y_coord;
    private final int amount;

    private double a = 0;

    public GalaxyParticleEffect(String key, Map<String, Double> modifiers, ParticleInformation information) {
        super(key, information);

        speed = resolveModifier(modifiers, "speed") * .2;
        height = resolveModifier(modifiers, "height");
        r_speed = resolveModifier(modifiers, "rotation-speed");
        y_coord = resolveModifier(modifiers, "y-coord");
        amount = (int) resolveModifier(modifiers, "amount");
    }

    public GalaxyParticleEffect(ConfigObject obj) {
        super(obj);

        speed = obj.getDouble("speed") * .2;
        height = obj.getDouble("height");
        r_speed = obj.getDouble("rotation-speed");
        y_coord = obj.getDouble("y-coord");
        amount = obj.getInt("amount");
    }

    @Override
    public ParticleEffectType getType() {
        return ParticleEffectType.GALAXY;
    }

    @Override
    public void tick() {
        Location loc = player.getLocation();
        for (int k = 0; k < amount; k++) {
            double a = this.a + Math.PI * 2 * k / amount;
            particle.display(loc.clone().add(0, height, 0), 0, (float) Math.cos(a), y_coord, (float) Math.sin(a), speed);
        }

        a += Math.PI / 24 * r_speed;
        if (a > Math.PI * 2) a -= Math.PI * 2;
    }
}
