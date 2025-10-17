package io.lumine.mythic.lib.data.queue;

import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import io.lumine.mythic.lib.data.DataFetcher;
import io.lumine.mythic.lib.data.DataLoadResult;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.util.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DataLoadQueue<H extends SynchronizedDataHolder> extends DataQueue<H> {
    public DataLoadQueue(@NotNull SynchronizedDataManager<H, ?> manager) {
        super(manager);
    }

    @NotNull
    public CompletableFuture<Void> enqueue(@NotNull H playerData, @Nullable Event reason) {
        final var record = new Record(playerData, reason);
        this.enqueue(record);
        return record.future;
    }

    @Override
    protected void processRecord(QueueRecord recordI) {
        final var record = (Record) recordI;

        final var lookup = record.playerData.getMMOPlayerData().isLookup();

        // Load player data
        final var dataFetcher = new DataFetcher<>(manager, record.playerData);
        final var result = dataFetcher.run();

        // No success, do nothing
        if (result.type != DataLoadResult.Type.SUCCESS) {
            record.future.complete(null);
            return;
        }

        // Data is loaded, back to server thread.
        Tasks.runSync(owning, () -> {

            if (!lookup) {

                // Player could go offline while transitioning to main thread
                // TODO improve thread safety
                if (!record.playerData.getMMOPlayerData().isOnline()) {
                    record.future.complete(null); // Complete future
                    return;
                }

                record.playerData.markSessionReady(); // Mark as ready
                Bukkit.getPluginManager().callEvent(new SynchronizedDataLoadEvent(manager, record.playerData, record.reason));
            }

            record.future.complete(null); // Complete future
        });
    }

    protected class Record extends QueueRecord {
        public final @Nullable Event reason;

        public Record(H playerData, @Nullable Event reason) {
            super(playerData);

            this.reason = reason;
        }
    }
}
