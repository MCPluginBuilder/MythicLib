package io.lumine.mythic.lib.hologram.factory;

import com.sainttx.holograms.HologramPlugin;
import com.sainttx.holograms.api.HologramManager;
import com.sainttx.holograms.api.line.TextLine;
import io.lumine.mythic.lib.hologram.Hologram;
import io.lumine.mythic.lib.hologram.HologramFactory;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles compatibility with Holograms using HologramFactory from LumineUtils.
 *
 * @author indyuce
 */
public class HologramsHologramFactory implements HologramFactory {
    private final HologramManager hologramManager = JavaPlugin.getPlugin(HologramPlugin.class).getHologramManager();

    /*public void displayIndicator(Location loc, String message, Player player) {
		Hologram hologram = new Hologram("MMOItems_" + UUID.randomUUID().toString(), loc);
		hologramManager.addActiveHologram(hologram);
		hologram.addLine(new TextLine(hologram, message));
		Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, () -> hologramManager.deleteHologram(hologram), 20);
	} */

    @NotNull
    public Hologram newHologram(@NotNull Location loc, @NotNull List<String> lines) {
        return new HologramImpl(loc, lines);
    }

    private final class HologramImpl extends Hologram {
        private final com.sainttx.holograms.api.Hologram holo;
        private boolean spawned = true;

        public HologramImpl(Location loc, List<String> list) {
            holo = new com.sainttx.holograms.api.Hologram("MythicLib-" + UUID.randomUUID(), loc);
            for (String str : list)
                holo.addLine(new TextLine(holo, str));
        }

        @Override
        public List<String> getLines() {
            return holo.getLines().stream().map(line -> line.getRaw()).collect(Collectors.toList());
        }

        @Override
        public Location getLocation() {
            return holo.getLocation();
        }

        @Override
        public void updateLines(@NotNull List<String> list) {
            throw new RuntimeException("Adding lines is not supported");
        }

        @Override
        public void despawn() {
            Validate.isTrue(spawned, "Hologram is already despawned");
            hologramManager.deleteHologram(holo);
            spawned = false;
        }

        @Override
        public boolean isSpawned() {
            return spawned;
        }

        @Override
        public void updateLocation(@NotNull Location loc) {
            holo.teleport(loc);
        }
    }
}
