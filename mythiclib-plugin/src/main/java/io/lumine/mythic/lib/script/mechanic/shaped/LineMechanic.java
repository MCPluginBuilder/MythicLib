package io.lumine.mythic.lib.script.mechanic.shaped;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.targeter.LocationTargeter;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.DoubleFormula;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Ticks from point A to point B
 */
public class LineMechanic extends Mechanic {
    private final DoubleFormula step;
    private final LocationTargeter source, target;

    private final Script onStart, onTick, onEnd;

    private final boolean instant;
    private final int pointsPerTick;

    public LineMechanic(ConfigObject config) {
        config.validateKeys("source", "target");

        source = config.getLocationTargeter("source");
        target = config.getLocationTargeter("target");

        step = config.getDoubleFormula("step", DoubleFormula.constant(.5));
        instant = config.getBoolean("instant", false);
        pointsPerTick = config.getInt("points_per_tick", 1);
        Validate.isTrue(pointsPerTick > 0, "PPT must be strictly positive");

        onStart = config.getScriptOrNull("start");
        onTick = config.getScriptOrNull("tick");
        onEnd = config.getScriptOrNull("end");
    }

    @Override
    public void cast(SkillMetadata meta) {
        Location source = this.source.findTargets(meta).get(0);
        for (Location loc : this.target.findTargets(meta))
            cast(meta, source, loc);
    }

    public void cast(SkillMetadata meta, Location source, Location target) {
        if (onStart != null) onStart.cast(meta.clone(source.clone()));

        final double step = LineMechanic.this.step.evaluate(meta);
        final Vector diff = target.clone().subtract(source).toVector();
        final double maxDist = diff.length();

        // Draws line instantaneously
        if (instant) {
            for (double dist = 0; dist < maxDist; dist += step) {
                final Location inter = source.clone().add(diff.clone().multiply(dist / maxDist));
                onTick.cast(meta.clone(inter));
            }
            if (onEnd != null) onEnd.cast(meta.clone(target.clone()));
            return;
        }

        // Draws line in time
        new BukkitRunnable() {

            // Distance counter
            double dist = 0;

            public void run() {
                for (int i = 0; i < pointsPerTick; i++) {
                    if (dist > maxDist) {
                        cancel();
                        if (onEnd != null) onEnd.cast(meta.clone(target.clone()));
                        return;
                    }

                    final Location inter = source.clone().add(diff.clone().multiply(dist / maxDist));
                    onTick.cast(meta.clone(inter));
                    dist += step;
                }
            }
        }.runTaskTimer(MythicLib.plugin, 0, 1);
    }
}
