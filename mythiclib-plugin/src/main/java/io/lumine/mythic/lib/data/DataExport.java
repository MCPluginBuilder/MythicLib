package io.lumine.mythic.lib.data;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.FileUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Used to:
 * - export data from flat to SQL storage.
 * - apply changes to flat/SQL datafiles directly without needing the player to login.
 */
public class DataExport<H extends SynchronizedDataHolder, O extends OfflineDataHolder> {
    private final SynchronizedDataManager<H, O> manager;
    private final CommandSender output;

    @Nullable
    private Runnable callback;

    /**
     * Amount of requests generated every batch
     */
    private static final int BATCH_AMOUNT = 50;

    /**
     * Period of batches in ticks
     */
    private static final int BATCH_PERIOD = 20;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#");

    public DataExport(@NotNull SynchronizedDataManager<H, O> manager,
                      @NotNull CommandSender output) {
        this.manager = manager;
        this.output = output;
    }

    public void setCallback(@Nullable Runnable callback) {
        this.callback = callback;
    }

    public boolean start(@NotNull Supplier<SynchronizedDataHandler<H, O>> source,
                         @NotNull Supplier<SynchronizedDataHandler<H, O>> target) {

        // Make sure no players are online
        if (!manager.getLoaded().isEmpty()) {
            output.sendMessage("Please make sure no players are logged in when using this command. " +
                    "If you are still seeing this message, please restart your server and " +
                    "execute this command before any player logs in.");
            return false;
        }

        // Collect IDs from flat storage
        final List<UUID> playerIds = Arrays.stream(FileUtils.getFile(manager.getOwningPlugin(), "userdata").listFiles())
                .map(file -> UUID.fromString(file.getName().split("\\.", 2)[0]))
                .collect(Collectors.toList());

        // Initialize fake SQL & YAML data provider
        final SynchronizedDataHandler<H, O> targetHandler;
        final SynchronizedDataHandler<H, O> sourceHandler;
        try {
            targetHandler = target.get();
            sourceHandler = source.get();

            // Setup both
            sourceHandler.setup();
            targetHandler.setup();
        } catch (RuntimeException exception) {
            output.sendMessage("Could not initialize SQL/YAML provider (see console for stack trace): " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }

        final double timeEstimation = (double) playerIds.size() / BATCH_AMOUNT * BATCH_PERIOD / 20;
        output.sendMessage("Processing " + playerIds.size() + " player data(s).. See console for details");
        output.sendMessage("ETA: " + DECIMAL_FORMAT.format(timeEstimation) + "s");

        // Save player data
        new BukkitRunnable() {
            int errorCount = 0;
            int batchCounter = 0;

            @Override
            public void run() {
                for (int i = 0; i < BATCH_AMOUNT; i++) {
                    final int index = BATCH_AMOUNT * batchCounter + i;

                    /*
                     * Saving is done. Close connection to avoid memory
                     * leaks and output the results to the command executor
                     */
                    if (index >= playerIds.size()) {
                        cancel();

                        // Close both
                        sourceHandler.close();
                        targetHandler.close();

                        manager.getOwningPlugin().getLogger().log(Level.WARNING, "Processed " + playerIds.size() + " player data(s). Error Count: " + errorCount);
                        if (callback != null) callback.run();
                        return;
                    }

                    try {
                        final UUID playerId = playerIds.get(index);
                        final H offlinePlayerData = manager.newPlayerData(new MMOPlayerData(playerId));
                        sourceHandler.loadData(offlinePlayerData);
                        targetHandler.saveData(offlinePlayerData, false);
                    } catch (RuntimeException exception) {
                        errorCount++;
                        exception.printStackTrace();
                    }
                }

                batchCounter++;
            }
        }.runTaskTimerAsynchronously(manager.getOwningPlugin(), 0, BATCH_PERIOD);

        return true;
    }
}
