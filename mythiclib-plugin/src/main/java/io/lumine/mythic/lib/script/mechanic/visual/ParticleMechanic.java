package io.lumine.mythic.lib.script.mechanic.visual;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.script.mechanic.type.LocationMechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;

@MechanicMetadata
public class ParticleMechanic extends LocationMechanic {
    private final Particle particle;
    private final int amount;
    private final double speed, xoffset, yoffset, zoffset;

    // Special data
    private final Particle.DustOptions dustOptions;
    private final BlockData blockData;

    private static final Particle.DustOptions DEFAULT_DUST_OPTIONS = new Particle.DustOptions(Color.RED, 1);
    private static final BlockData DEFAULT_BLOCK_DATA = Material.DIRT.createBlockData();

    public ParticleMechanic(ConfigObject config) {
        super(config);

        config.validateKeys("particle");

        particle = Particle.valueOf(UtilityMethods.enumName(config.getString("particle")));
        amount = config.getInt("amount", 1);
        speed = config.getDouble("speed", 0);
        xoffset = config.getDouble("x", 0);
        yoffset = config.getDouble("y", 0);
        zoffset = config.getDouble("z", 0);

        // Special options
        dustOptions = config.contains("color") ? new Particle.DustOptions(loadColor(config.getObject("color")), (float) config.getDouble("size", 1)) : DEFAULT_DUST_OPTIONS;
        blockData = config.contains("block") ? Material.valueOf(UtilityMethods.enumName(config.getString("block"))).createBlockData() : DEFAULT_BLOCK_DATA;
    }

    @Override
    public void cast(SkillMetadata meta, Location loc) {

        // Redstone particle
        if (particle.getDataType() == Particle.DustOptions.class)
            meta.getCaster().getPlayer().getWorld().spawnParticle(particle, loc, amount, xoffset, yoffset, zoffset, speed, dustOptions);

            // Block particle
        else if (particle.getDataType() == BlockData.class)
            meta.getCaster().getPlayer().getWorld().spawnParticle(particle, loc, amount, xoffset, yoffset, zoffset, speed, blockData);

            // Default particle
        else
            meta.getCaster().getPlayer().getWorld().spawnParticle(particle, loc, amount, xoffset, yoffset, zoffset, speed);
    }

    private Color loadColor(ConfigObject obj) {
        Validate.isTrue(obj.contains("red") && obj.contains("green") && obj.contains("blue"), "Color must have red, green and blue");
        return Color.fromRGB(obj.getInt("red"), obj.getInt("green"), obj.getInt("blue"));
    }
}