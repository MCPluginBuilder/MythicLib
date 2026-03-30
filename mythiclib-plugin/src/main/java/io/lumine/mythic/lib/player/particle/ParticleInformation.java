package io.lumine.mythic.lib.player.particle;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enough information to fully display one particle.
 *
 * @author jules
 */
public class ParticleInformation {

    // Generic particle data
    private final Particle particle;
    private final int amount;
    private final double xOffset, yOffset, zOffset, speed;

    // Custom data
    @Nullable
    private final Object data;

    public ParticleInformation(Particle particle, int amount, float speed, double offset, @Nullable Object data) {
        this(particle, amount, speed, offset, offset, offset, data);
    }

    public ParticleInformation(Particle particle, int amount, float speed, double xOffset, double yOffset, double zOffset, @Nullable Object data) {
        this.particle = particle;
        this.amount = amount;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.speed = speed;
        this.data = data;
    }

    /**
     * Displays particle with default parameters
     */
    public void display(Location loc) {
        loc.getWorld().spawnParticle(particle, loc, this.amount, this.xOffset, this.yOffset, this.zOffset, speed, this.data);
    }

    /**
     * Displays particle at target location and overrides default speed
     */
    public void display(Location loc, double speed) {
        loc.getWorld().spawnParticle(particle, loc, amount, this.xOffset, this.yOffset, this.zOffset, speed, this.data);
    }

    /**
     * Displays particle at target location and overrides amount, offset and particle speed
     */
    public void display(Location loc, int amount, double xOffset, double yOffset, double zOffset, double speed) {
        loc.getWorld().spawnParticle(particle, loc, amount, xOffset, yOffset, zOffset, speed, this.data);
    }

    //region Static methods

    /**
     * When reading from a YAML config file or
     * from a mechanic line config.
     *
     * @param obj Either a string or YML configuration section
     * @return Read particle information
     */
    @NotNull
    public static ParticleInformation fromConfig(@NotNull Object obj) {
        Validate.notNull(obj, "Cannot read particle from null object");

        // String
        if (obj instanceof String) {
            final var particle = UtilityMethods.prettyValueOf(Particle::valueOf, (String) obj, "No particle with name '%s'");
            return of(particle);
        }

        // Config section
        if (obj instanceof ConfigurationSection) {
            return fromConfig(new ConfigSectionObject((ConfigurationSection) obj));
        }

        // Line config object
        if (obj instanceof ConfigObject) {
            final var config = (ConfigObject) obj;
            final var particle = config.parse(Parsers.PARTICLE, "name", "particle");
            final var rOffset = config.getDouble("offset", 0);
            final var speed = (float) config.getDouble("speed", 0);
            final var amount = config.getInt("amount", 1);
            final var dataType = particle.getDataType();
            final Object data;

            ////////////////
            // Dust
            ////////////////
            if (dataType == Particle.DustOptions.class) {
                var color = readColorFromConfig(config);
                var size = (float) config.getDouble("size", 1);
                data = new Particle.DustOptions(color, size);
            }

            ///////////////
            // FLASH on 1.21+
            ///////////////
            else if (dataType == Color.class) {
                data = readColorFromConfig(config);
            }

            ///////////////
            // Blocks
            ///////////////
            else if (dataType == BlockData.class) {
                // Use fallback if not provided
                var material = config.parse(Material.DIRT, Material::valueOf, "material", "block");
                data = material.createBlockData();
            }

            ///////////////
            // Spell (1.21.10+)
            ///////////////
            else if (MythicLib.plugin.getVersion().isAbove(1, 21, 9) && dataType == Particle.Spell.class) {
                // Fallback to white color
                final ConfigObject colorObj = config.contains("color") ? config.getObject("color") : config;
                final int red = colorObj.getInt("red", 255);
                final var green = colorObj.getInt("green", 255);
                final int blue = colorObj.getInt("blue", 255);
                final var power = (float) config.getDouble("power", 1);
                data = new Particle.Spell(Color.fromRGB(red, green, blue), power);
            }

            // Any other
            else data = null;

            return new ParticleInformation(particle, amount, speed, rOffset, data);
        }

        throw new IllegalArgumentException("Cannot read particle from " + obj.getClass().getSimpleName());
    }

    @NotNull
    private static Color readColorFromConfig(ConfigObject config) {
        // Fallback to color "red" (255, 0, 0)

        ConfigObject colorObj;
        if (config.contains("color")) {
            try {
                // Try parse as Hex color code
                var asString = config.getString("color");
                var asInteger = Integer.parseInt(asString, 16);
                return Color.fromRGB(asInteger);
            } catch (Exception exception) {
                colorObj = config.getObject("color");
            }
        } else colorObj = config;

        var red = colorObj.getInt("red", 255);
        var green = colorObj.getInt("green", 0);
        var blue = colorObj.getInt("blue", 0);

        return Color.fromRGB(red, green, blue);
    }

    /**
     * Used by MMOItems to display projectile (arrows/tridents) particles
     * and therefore require all the parameters input.
     *
     * @param object Json object to read data from
     */
    @NotNull
    public static ParticleInformation fromJson(JsonObject object) {
        var particle = Particle.valueOf(object.get("Particle").getAsString());
        var amount = object.get("Amount").getAsInt();
        var rOffset = object.get("Offset").getAsDouble();
        var speed = object.has("Speed") ? object.get("Speed").getAsFloat() : 0;
        final var dataType = particle.getDataType();

        final Object data;

        // MMOItems atm only supports dust particles.
        if (dataType == Particle.DustOptions.class) {
            // mmoitems > "Arrow particles" format
            // Note that "Item Particles" uses a slightly different format
            final Color color = Color.fromRGB(object.get("Red").getAsInt(), object.get("Green").getAsInt(), object.get("Blue").getAsInt());
            data = new Particle.DustOptions(color, 1);
        }

        // Use fallback
        else data = getFallbackDataObject(dataType);

        return new ParticleInformation(particle, amount, speed, rOffset, data);
    }

    @NotNull
    public static ParticleInformation of(Particle particle) {
        var amount = 1;
        var speed = 0;
        var rOffset = 0;
        var data = getFallbackDataObject(particle.getDataType());
        return new ParticleInformation(particle, amount, speed, rOffset, data);
    }

    @Nullable
    public static Object getFallbackDataObject(Class<?> dataType) {
        if (dataType == Void.class) return null;
        if (dataType == Particle.DustOptions.class) return new Particle.DustOptions(Color.RED, 1);
        if (dataType == BlockData.class) return Material.DIRT.createBlockData();
        if (dataType == Color.class) return Color.WHITE;
        if (MythicLib.plugin.getVersion().isAbove(1, 21, 9) && dataType == Particle.Spell.class) return new Particle.Spell(Color.WHITE, 1);
        throw new IllegalArgumentException("Unsupported data class " + dataType.getSimpleName());
    }

    //endregion
}
