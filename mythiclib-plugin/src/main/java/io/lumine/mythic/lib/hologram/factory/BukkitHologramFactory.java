package io.lumine.mythic.lib.hologram.factory;

import com.google.common.base.Preconditions;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.hologram.Hologram;
import io.lumine.mythic.lib.hologram.HologramFactory;
import io.lumine.mythic.lib.listener.option.GameIndicators;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BukkitHologramFactory implements HologramFactory {
    public BukkitHologramFactory() {
        Validate.isTrue(MythicLib.plugin.getVersion().isAbove(1, 19, 4), "Text displays are only available on 1.19.4+");
    }

    @NotNull
    public Hologram newHologram(@NotNull Location loc, @NotNull List<String> lines) {
        return new HologramImpl(loc, lines);
    }

    private static final class HologramImpl extends Hologram {
        private final List<String> lines = new ArrayList<>();
        private final List<TextDisplay> spawnedEntities = new ArrayList<>();

        private Location loc;
        private boolean spawned = false;

        HologramImpl(@NotNull Location loc, @NotNull List<String> lines) {
            this.loc = Objects.requireNonNull(loc, "Location cannot be null").clone();
            this.updateLines(lines);

            spawn();
        }

        @Override
        public List<String> getLines() {
            return lines;
        }

        private static final double LINE_OFFSET = .25;

        private void spawn() {

            // Add new lines
            Location clone = loc.clone();
            for (String line : lines) {
                final TextDisplay as = clone.getWorld().spawn(clone, TextDisplay.class);
                as.setBillboard(Display.Billboard.CENTER);
                // as.setInterpolationDuration(INTERPOLATION_DURATION);

                this.spawnedEntities.add(as);
                as.setText(line);

                clone.subtract(0, LINE_OFFSET, 0);
            }

            this.spawned = true;
        }

        @Override
        public void despawn() {
            this.spawnedEntities.forEach(Entity::remove);
            this.spawnedEntities.clear();
            this.spawned = false;
        }

        @Override
        public boolean isSpawned() {
            return spawned;
        }

        @Override
        public void updateLocation(@NotNull Location newLoc) {
            Validate.isTrue(spawned, "Hologram is not spawned");
            if (loc.distanceSquared(newLoc) < EPSILON) return;
            loc = newLoc.clone();

            Location clone = loc.clone();
            for (TextDisplay textDisplay : getSpawnedEntities()) {
                textDisplay.teleport(clone);
                clone.subtract(0, LINE_OFFSET, 0);
            }
        }

        private static final double EPSILON = 1e-5;

        /*
        private static final int OVERSHOOT = 3;
        private static final int INTERPOLATION_DURATION = 3 * OVERSHOOT;
        private static final AxisAngle4f NULL_ROTATION = new AxisAngle4f(0, 0, 1, 0);
        private static final Vector3f VECTOR_ONE = new Vector3f(1, 1, 1);
        */

        /**
         * Move around holograms using transforms and no teleports. This
         * is much better for server tick and animation smoothness.
         */
        @Override
        public void flyOut(@NotNull GameIndicators settings, @NotNull Vector dir) {

            // Teleport duration is not implemented in 1.20.1 and below
            Validate.isTrue(MythicLib.plugin.getVersion().isAbove(1, 20, 2), "Moving indicators is only available in 1.20.2 and above");

            for (TextDisplay td : getSpawnedEntities())
                td.setTeleportDuration((int) settings.tickPeriod);

            super.flyOut(settings, dir);
            /*
            Validate.isTrue(spawned, "Hologram is not spawned");

            new BukkitRunnable() {

                @Override
                public void run() {

                    // Transformation applied to text displays
                    Transformation transf = new Transformation(toJoml(newLoc.toVector().subtract(initLoc.toVector()).multiply(OVERSHOOT)), NULL_ROTATION, VECTOR_ONE, NULL_ROTATION);

                    // Update delay and transform
                    for (TextDisplay textDisplay : getSpawnedEntities()) {
                        textDisplay.setInterpolationDelay(0);
                        textDisplay.setTransformation(transf);
                    }
                }
            }.runTaskTimer(MythicLib.plugin, 0, settings.tickPeriod);
            */
        }

        /*
        private Vector3f toJoml(Vector vec) {
            return new Vector3f((float) vec.getX(), (float) vec.getY(), (float) vec.getZ());
        }
        */

        @Override
        public void updateLines(@Nonnull List<String> lines) {
            Objects.requireNonNull(lines, "lines");
            Preconditions.checkArgument(!lines.isEmpty(), "Lines cannot be empty");
            for (String line : lines)
                Preconditions.checkArgument(line != null, "Null line");

            this.lines.clear();
            this.lines.addAll(lines);
        }

        @Override
        public Location getLocation() {
            return loc;
        }

        public List<TextDisplay> getSpawnedEntities() {
            return this.spawnedEntities;
        }
    }
}
