package io.lumine.mythic.lib.hologram.factory;

import io.lumine.mythic.lib.hologram.Hologram;
import io.lumine.mythic.lib.hologram.HologramFactory;
import io.lumine.mythic.lib.listener.option.GameIndicators;
import me.arasple.mc.trhologram.api.TrHologramAPI;
import me.arasple.mc.trhologram.api.hologram.HologramBuilder;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Handles compatibility with TrHologram
 *
 * @author TUCAOEVER, Jules
 */
public class TrHologramFactory implements HologramFactory {

  /*  public void displayIndicator(final Location loc, final String format, final Player player) {
        Hologram hologram = TrHologramAPI.builder(loc)
                .append(format)
                .build();
        Bukkit.getScheduler().scheduleSyncDelayedTask(MythicLib.plugin, hologram::destroy, 20L);
    }*/

    @NotNull
    public Hologram newHologram(@NotNull Location loc, @NotNull List<String> lines) {
        return new HologramImpl(loc, lines);
    }

    static final class HologramImpl extends Hologram {
        private final me.arasple.mc.trhologram.module.display.Hologram holo;
        private final List<String> lines;
        private boolean spawned = true;

        public HologramImpl(Location loc, List<String> lines) {
            HologramBuilder builder = TrHologramAPI.builder(loc);
            for (String line : lines)
                builder.append(line);
            holo = builder.build();
            this.lines = lines;
        }

        @Override
        public boolean isSpawned() {
            return spawned;
        }

        @Override
        public List<String> getLines() {
            return lines;
        }

        @Override
        public void updateLines(@NotNull List<String> list) {
            throw new RuntimeException("Updating lines is not supported");
        }

        @Override
        public Location getLocation() {
            return holo.getPosition().toLocation();
        }

        @Override
        public void updateLocation(Location loc) {
            // Not supported
        }

        @Override
        public void flyOut(@NotNull GameIndicators settings, @NotNull Vector dir) {
            // Not supported
        }

        @Override
        public void despawn() {
            Validate.isTrue(spawned, "Hologram is already despawned");
            holo.destroy();
            spawned = false;
        }
    }
}
