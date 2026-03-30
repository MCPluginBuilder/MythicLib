package io.lumine.mythic.lib.hologram.factory;

import com.google.common.base.Preconditions;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.hologram.Hologram;
import io.lumine.mythic.lib.hologram.HologramFactory;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class LegacyBukkitHologramFactory implements HologramFactory {

    public LegacyBukkitHologramFactory() {
        Bukkit.getScheduler().runTaskLater(MythicLib.plugin, this::clearPreviousEntities, 20L);
    }

    @NotNull
    public Hologram newHologram(@NotNull Location loc, @NotNull List<String> lines) {
        return new HologramImpl(loc, lines);
    }

    private void clearPreviousEntities() {
        // Safeguard in case of server crash
        for (var world : Bukkit.getWorlds())
            for (var display : world.getEntitiesByClasses(TextDisplay.class))
                if (display.getPersistentDataContainer().has(PDC_KEY)) display.remove();
    }

    private static final NamespacedKey PDC_KEY = new NamespacedKey(MythicLib.plugin, "hologram");

    static final class HologramImpl extends Hologram {
        private static final Method SET_CAN_TICK;
        private Location loc;
        private final List<String> lines = new ArrayList<>();
        private final List<ArmorStand> spawnedEntities = new ArrayList<>();
        private boolean spawned = false;

        HologramImpl(Location loc, List<String> lines) {
            this.loc = Objects.requireNonNull(loc, "position");
            this.updateLines(lines);

            spawn();
        }

        @Override
        public List<String> getLines() {
            return lines;
        }

        private Location getNewLinePosition() {
            if (this.spawnedEntities.isEmpty()) {
                return this.loc;
            } else {
                ArmorStand last = (ArmorStand) this.spawnedEntities.get(this.spawnedEntities.size() - 1);
                return last.getLocation().subtract(0.0D, 0.25D, 0.0D);
            }
        }

        public void spawn() {
            int linesSize = this.lines.size();
            int spawnedSize = this.spawnedEntities.size();
            int i;
            ArmorStand as;
            if (linesSize < spawnedSize) {
                i = spawnedSize - linesSize;

                for (int j = 0; j < i; ++j) {
                    as = (ArmorStand) this.spawnedEntities.remove(this.spawnedEntities.size() - 1);
                    as.remove();
                }
            }

            for (i = 0; i < this.lines.size(); ++i) {
                String line = (String) this.lines.get(i);
                if (i >= this.spawnedEntities.size()) {
                    Location loc = this.getNewLinePosition();
                    Chunk chunk = loc.getChunk();
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }

                    loc.getWorld().getNearbyEntities(loc, 1.0D, 1.0D, 1.0D).forEach((e) -> {
                        if (e.getType() == EntityType.ARMOR_STAND && locationsEqual(e.getLocation(), loc)) {
                            e.remove();
                        }

                    });
                    as = (ArmorStand) loc.getWorld().spawn(loc, ArmorStand.class);
                    as.getPersistentDataContainer().set(PDC_KEY, PersistentDataType.BOOLEAN, true);
                    as.setSmall(true);
                    as.setMarker(true);
                    as.setArms(false);
                    as.setBasePlate(false);
                    as.setGravity(false);
                    as.setVisible(false);
                    as.setCustomName(line);
                    as.setCustomNameVisible(true);
                    as.setAI(false);
                    as.setCollidable(false);
                    as.setInvulnerable(true);
                    if (SET_CAN_TICK != null) {
                        try {
                            SET_CAN_TICK.invoke(as, false);
                        } catch (Exception var9) {
                            var9.printStackTrace();
                        }
                    }

                    this.spawnedEntities.add(as);
                } else {
                    as = (ArmorStand) this.spawnedEntities.get(i);
                    if (as.getCustomName() == null || !as.getCustomName().equals(line)) {
                        as.setCustomName(line);
                    }
                }
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
            if (!this.spawned) {
                return false;
            } else {
                Iterator<ArmorStand> var1 = this.spawnedEntities.iterator();

                ArmorStand stand;
                do {
                    if (!var1.hasNext()) {
                        return true;
                    }

                    stand = (ArmorStand) var1.next();
                } while (stand.isValid());

                return false;
            }
        }

        @Override
        public void updateLocation(@NotNull Location loc) {
            Objects.requireNonNull(loc, "position");
            if (!this.loc.equals(loc)) {
                this.loc = loc;
                if (!this.isSpawned()) {
                    this.spawn();
                } else {
                    double offset = 0.0D;

                    for (Iterator<ArmorStand> var4 = this.getSpawnedEntities().iterator(); var4.hasNext(); offset += 0.25D) {
                        ArmorStand as = (ArmorStand) var4.next();
                        as.teleport(loc.add(0.0D, offset, 0.0D));
                    }
                }

            }
        }

        @Override
        public void updateLines(@Nonnull List<String> lines) {
            Objects.requireNonNull(lines, "lines");
            Preconditions.checkArgument(!lines.isEmpty(), "lines cannot be empty");

            for (String line : lines) {
                Preconditions.checkArgument(line != null, "null line");
            }

            this.lines.clear();
            this.lines.addAll(lines);
        }


        private static boolean locationsEqual(Location l1, Location l2) {
            return Double.doubleToLongBits(l1.getX()) == Double.doubleToLongBits(l2.getX()) && Double.doubleToLongBits(l1.getY()) == Double.doubleToLongBits(l2.getY()) && Double.doubleToLongBits(l1.getZ()) == Double.doubleToLongBits(l2.getZ());
        }

        @Override
        public Location getLocation() {
            return loc;
        }

        public List<ArmorStand> getSpawnedEntities() {
            return this.spawnedEntities;
        }

        static {
            Method setCanTick = null;

            try {
                setCanTick = ArmorStand.class.getDeclaredMethod("setCanTick", Boolean.TYPE);
            } catch (Exception ignored) {
            }

            SET_CAN_TICK = setCanTick;
        }
    }
}
