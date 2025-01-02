package io.lumine.mythic.lib.hologram.factory;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.hologram.Hologram;
import io.lumine.mythic.lib.hologram.HologramFactory;
import io.lumine.mythic.lib.util.lang3.Validate;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Handles compatibility with HolographicDisplays using HologramFactory from LumineUtils.
 * <p>
 * HolographicDisplays is by far the best hologram plugin we
 * can use to display indicators.
 *
 * @author indyuce
 */
public class HDHologramFactory implements HologramFactory {

   /* public void displayIndicator(Location loc, String format, Player player) {
        Hologram hologram = HologramsAPI.createHologram(MythicLib.plugin, loc);
        hologram.appendTextLine(format);
        if (player != null)
            hologram.getVisibilityManager().hideTo(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(MythicLib.plugin, hologram::delete, 20);
    }*/

    @NotNull
    public Hologram newHologram(@NotNull Location loc, @NotNull List<String> lines) {
        return new HologramImpl(loc, lines);
    }

    private static final class HologramImpl extends Hologram {
        private final me.filoghost.holographicdisplays.api.hologram.Hologram holo;
        private final List<String> lines;
        private boolean spawned = true;

        public HologramImpl(Location loc, List<String> list) {
            holo = HolographicDisplaysAPI.get(MythicLib.plugin).createHologram(loc);
            this.lines = list;
            for (String line : lines)
                holo.getLines().appendText(line);
        }

        @Override
        public List<String> getLines() {
            return lines;
        }

        @Override
        public void updateLines(@NotNull List<String> list) {
            throw new RuntimeException("Adding lines is not supported");
        }

        @Override
        public Location getLocation() {
            return holo.getPosition().toLocation();
        }

        @Override
        public void updateLocation(@NotNull Location loc) {
            holo.setPosition(loc);
        }

        @Override
        public void despawn() {
            Validate.isTrue(spawned, "Hologram is already despawned");
            holo.delete();
            spawned = false;
        }

        @Override
        public boolean isSpawned() {
            return spawned;
        }
    }
}
