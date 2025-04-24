package io.lumine.mythic.lib.script.targeter.entity;

import io.lumine.mythic.lib.script.targeter.EntityTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class NearbyEntitiesTargeter implements EntityTargeter {
    private final DoubleFormula radius, height;
    private final boolean source, ignoreCaster;

    public NearbyEntitiesTargeter(ConfigObject config) {
        config.validateKeys("radius");

        source = config.getBoolean("source", false);
        radius = new DoubleFormula(config.getString("radius"));
        height = config.contains("height") ? new DoubleFormula(config.getString("height")) : null;
        ignoreCaster = config.getBoolean("ignore_caster", true);
    }

    @Override
    public List<Entity> findTargets(SkillMetadata meta) {

        // Source location
        Location loc = meta.getSkillLocation(source);

        final double rad = radius.evaluate(meta), height = this.height == null ? rad : this.height.evaluate(meta);
        List<Entity> list = new ArrayList<>(loc.getWorld().getNearbyEntities(loc, rad, height, rad));

        // Ignore entity
        if (ignoreCaster) list.remove(meta.getSkillEntity(source));

        return list;
    }
}
