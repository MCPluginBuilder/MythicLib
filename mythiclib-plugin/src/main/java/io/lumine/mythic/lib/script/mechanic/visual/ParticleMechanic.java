package io.lumine.mythic.lib.script.mechanic.visual;

import io.lumine.mythic.lib.player.particle.ParticleInformation;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.LocationMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;

@MechanicMetadata
public class ParticleMechanic extends LocationMechanic {
    private final ParticleInformation particleInformation;
    private final int amount;
    private final double speed, xOffset, yOffset, zOffset;

    public ParticleMechanic(ConfigObject config) {
        super(config);

        particleInformation = ParticleInformation.fromConfig(config);

        // TODO fully merge these options with ParticleInformation
        // atm ParticleInformation only checks particle and data type.
        amount = config.getInt("amount", 1);
        speed = config.getDouble("speed", 0);
        xOffset = config.getDouble("x", 0);
        yOffset = config.getDouble("y", 0);
        zOffset = config.getDouble("z", 0);
    }

    @Override
    public void cast(SkillMetadata meta, Location loc) {
        this.particleInformation.display(loc, amount, xOffset, yOffset, zOffset, speed);
    }
}