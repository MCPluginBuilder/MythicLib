package io.lumine.mythic.lib.version;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class SpigotPlugin {
    private final JavaPlugin plugin;
    private final int id;

    private String version;

    public SpigotPlugin(int id, JavaPlugin plugin) {
        this.plugin = plugin;
        this.id = id;
    }

    /**
     * The request is executed asynchronously as not to block the main thread.
     */
    public void checkForUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final var connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + id).openConnection();
                connection.setRequestMethod("GET");
                version = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            } catch (Exception throwable) {
                plugin.getLogger().log(Level.INFO, "Could not check latest plugin version: " + throwable.getMessage());
                return;
            }

            if (!isOutdated(version, plugin.getDescription().getVersion())) return;

            plugin.getLogger().log(Level.INFO, "A new build is available: " + version + " (you are running " + plugin.getDescription().getVersion() + ")");
            plugin.getLogger().log(Level.INFO, "Download it here: " + getResourceUrl());

            /*
             * Registers the event to notify op players when they
             * join only if the corresponding option is enabled
             */
            if (plugin.getConfig().getBoolean("update-notify"))
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().registerEvents(new Listener() {
                    @EventHandler(priority = EventPriority.MONITOR)
                    public void onPlayerJoin(PlayerJoinEvent event) {
                        Player player = event.getPlayer();
                        if (player.hasPermission(plugin.getName().toLowerCase() + ".update-notify"))
                            getOutOfDateMessage().forEach(msg -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg)));
                    }
                }, plugin));
        });
    }

    private static boolean isOutdated(@NotNull String internetVersion, @NotNull String installedVersion) {
        final var publik = parseVersion(internetVersion);
        final var local = parseVersion(installedVersion);

        for (var i = 0; i < Math.max(publik.length, local.length); i++) {
            final var publicIdx = idx(publik, i);
            final var localIdx = idx(local, i);

            if (publicIdx < localIdx) return false;
            if (publicIdx > localIdx) return true;
        }

        return false;
    }

    private static int idx(int[] version, int index) {
        return index >= version.length ? 0 : version[index];
    }

    private static int[] parseVersion(@NotNull String input) {
        input = input.split("-")[0]; // Remove suffixes like "-SNAPSHOT"

        var parts = input.split("\\.");
        var version = new int[parts.length];
        for (int i = 0; i < parts.length; i++)
            try {
                version[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid version format: '" + input + "' is not an int");
            }

        return version;
    }

    @NotNull
    private List<String> getOutOfDateMessage() {
        return Arrays.asList("&8--------------------------------------------",
                "&a" + plugin.getName() + " " + version + " is available! (Running " + plugin.getDescription().getVersion() + ")",
                "&aDownload at: " + getResourceUrl(),
                "&7&oYou can disable this notification in the config file.",
                "&8--------------------------------------------");
    }

    @NotNull
    private String getResourceUrl() {
        return "https://www.spigotmc.org/resources/" + id + "/";
    }
}
