package io.lumine.mythic.lib.script.condition.location;

import io.lumine.mythic.lib.script.condition.type.LocationCondition;
import io.lumine.mythic.lib.script.util.Parsers;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.version.Biomes;
import org.bukkit.Location;
import org.bukkit.block.Biome;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Checks for the caster's current biome
 */
public class BiomeCondition extends LocationCondition {
    private final Set<Biome> biomes = new HashSet<>();

    public static final Function<String, Biome> PARSER_BIOME = Parsers.ofEnum(Biome.class, Biomes::fromName);

    public BiomeCondition(ConfigObject config) {
        super(config, true);

        for (String str : config.getString("name").split(","))
            biomes.add(config.parse(PARSER_BIOME, str));
    }

    @Override
    public boolean isMet(SkillMetadata meta, Location loc) {
        return biomes.contains(loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }
}