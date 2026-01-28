package io.lumine.mythic.lib.player.particle;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
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
 * <p>
 * {@link #rOffset} is used to replace the default three Bukkit parameters
 * used to display a pack of particle. Using only one parameter the particles
 * get displayed in a "ball" when considering the infinite norm
 *
 * @author jules
 */
public class ParticleInformation {

    // Generic particle data
    private final Particle particle;
    private final int amount;
    private final double rOffset, speed;

    // Custom data
    @Nullable
    private final Object data;

    public ParticleInformation(Particle particle, int amount, float speed, double rOffset, @Nullable Object data) {
        this.particle = particle;
        this.amount = amount;
        this.rOffset = rOffset;
        this.speed = speed;
        this.data = data;
    }

    /**
     * Displays particle with default parameters
     */
    public void display(Location loc) {
        display(loc, amount, rOffset, rOffset, rOffset, speed);
    }

    /**
     * Displays particle at target location and overrides default speed
     */
    public void display(Location loc, double speed) {
        display(loc, amount, rOffset, rOffset, rOffset, speed);
    }

    /**
     * Displays particle at target location and overrides amount, offset and particle speed
     */
    public void display(Location loc, int amount, double x, double y, double z, double speed) {
        loc.getWorld().spawnParticle(particle, loc, amount, x, y, z, speed, this.data);
    }

    //region Static methods

    /**
     * When reading from a YAML config file
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
            final var particle = UtilityMethods.prettyValueOf(Particle::valueOf, config.string("name", "particle"), "No particle with name '%s'");
            final var rOffset = config.getDouble("offset", 0);
            final var speed = (float) config.getDouble("speed", 0);
            final var amount = config.getInt("amount", 1);
            final var dataType = particle.getDataType();
            final Object data;

            ////////////////
            // Dust
            ////////////////
            if (dataType == Particle.DustOptions.class) {
                // Fallback to color "red" (255, 0, 0)
                final ConfigObject colorObj = config.contains("color") ? config.getObject("color") : config;
                final int red = colorObj.getInt("red", 255);
                final var green = colorObj.getInt("green", 0);
                final var blue = colorObj.getInt("blue", 0);
                final var size = (float) config.getDouble("size", 1);
                data = new Particle.DustOptions(Color.fromRGB(red, green, blue), size);
            }

            ///////////////
            // Blocks
            ///////////////
            else if (dataType == BlockData.class) {
                // Use fallback if not provided
                final Material mat = config.parse(Material.DIRT, Material::valueOf, "material");
                data = mat.createBlockData();
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

        ////////////////
        // Dust
        ////////////////
        if (dataType == Particle.DustOptions.class) {
            // mmoitems > "Arrow particles" format
            // Note that "Item Particles" uses a slightly different format
            final Color color = Color.fromRGB(object.get("Red").getAsInt(), object.get("Green").getAsInt(), object.get("Blue").getAsInt());
            data = new Particle.DustOptions(color, 1);
        }

        ///////////////
        // Blocks
        ///////////////
        else if (dataType == BlockData.class) {
            // mmoitems does not have block data
            // use fallback
            data = Material.DIRT.createBlockData();
        }

        ///////////////
        // Spell (1.21.10+)
        ///////////////
        else if (MythicLib.plugin.getVersion().isAbove(1, 21, 9) && dataType == Particle.Spell.class) {
            // mmoitems does not have spell color/power
            // use fallback
            data = new Particle.Spell(Color.WHITE, 1);
        }

        // Any other
        else data = null;

        return new ParticleInformation(particle, amount, speed, rOffset, data);
    }

    @NotNull
    public static ParticleInformation of(Particle particle) {
        final var dataType = particle.getDataType();

        final var amount = 1;
        final var speed = 0;
        final var rOffset = 0;
        final Object data = dataType == Particle.DustOptions.class ? new Particle.DustOptions(Color.RED, 1)
                : dataType == BlockData.class ? Material.DIRT.createBlockData()
                : MythicLib.plugin.getVersion().isAbove(1, 21, 9) && dataType == Particle.Spell.class ? new Particle.Spell(Color.WHITE, 1) : null;

        return new ParticleInformation(particle, amount, speed, rOffset, data);
    }

    //endregion
}
