package io.lumine.mythic.lib.hologram.factory;

import com.google.common.base.Preconditions;
import io.lumine.mythic.lib.hologram.HologramFactory;
import io.lumine.mythic.lib.util.IndicatorConfig;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ranet.coretools.features.hologram.HoloModes;
import ranet.coretools.features.hologram.Hologram;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class CoreToolsHologramFactory implements HologramFactory {

    @NotNull
    public io.lumine.mythic.lib.hologram.Hologram newHologram(@NotNull Location loc, @NotNull List<String> lines) {
        return new HologramImpl(loc, lines);
    }

    private static final class HologramImpl extends io.lumine.mythic.lib.hologram.Hologram {
        private final Hologram hologram;
        private final List<String> lines;

        @NotNull
        private Location currentLocation;
        private boolean spawned;

        HologramImpl(@NotNull Location loc, @NotNull List<String> lines) {
            Validate.notNull(lines, "Lines cannot be null");
            Validate.notEmpty(lines, "Lines cannot be empty");

            this.lines = lines;
            final var rawText = String.join("<newline>", lines);
            this.hologram = new Hologram("mythiclib", HoloModes.FIXED, loc, rawText).build();
            currentLocation = loc.clone();

            this.spawn();
        }

        private void spawn() {
            Validate.isTrue(!spawned, "Hologram is already spawned");
            spawned = true;

            this.hologram.spawn();
        }

        @Override
        public List<String> getLines() {
            return lines;
        }

        @Override
        public void despawn() {
            Validate.isTrue(spawned, "Hologram is already despawned");
            spawned = false;

            hologram.despawn();
        }

        @Override
        public boolean isSpawned() {
            return spawned;
        }

        @Override
        public void updateLocation(@NotNull Location newLoc) {
            Validate.isTrue(spawned, "Hologram is not spawned");

            if (currentLocation.distanceSquared(newLoc) < EPSILON) return;
            currentLocation = newLoc.clone();

            this.hologram.move(newLoc);
        }

        private static final double EPSILON = 1e-5;

        @Override
        public void flyOut(@NotNull IndicatorConfig settings, @NotNull Vector dir) {
            super.flyOut(settings, dir);
        }

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
            return currentLocation;
        }
    }
}
