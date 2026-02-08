package io.lumine.mythic.lib.data.queue;

import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import io.lumine.mythic.lib.util.Tasks;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DataSaveQueue<H extends SynchronizedDataHolder> extends DataQueue<H> {
    public DataSaveQueue(@NotNull SynchronizedDataManager<H, ?> manager) {
        super(manager);
    }

    @NotNull
    public CompletableFuture<Void> enqueue(@NotNull H playerData, @NotNull SessionUpdateReason reason) {
        final var record = new Record(playerData, reason);
        super.enqueue(record);
        return record.future;
    }

    @Override
    protected void processRecord(QueueRecord recordI) {
        final var record = (Record) recordI;

        // Save data
        database.saveData(record.playerData, record.reason);

        // Data saved. Back to server thread.
        Tasks.runSync(plugin, () -> {
            if (record.reason != SessionUpdateReason.AUTOSAVE) record.playerData.markSessionClosed();
            record.future.complete(null);
        });
    }

    public class Record extends QueueRecord {
        public final SessionUpdateReason reason;

        public Record(H playerData, SessionUpdateReason reason) {
            super(playerData, playerData.getEffectiveId(), new CompletableFuture<>(), 0);

            this.reason = reason;
        }
    }
}
