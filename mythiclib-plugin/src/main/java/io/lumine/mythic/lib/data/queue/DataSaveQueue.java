package io.lumine.mythic.lib.data.queue;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import io.lumine.mythic.lib.util.Tasks;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
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

        ///////////////////////////////
        // Save data
        ///////////////////////////////

        UtilityMethods.debug(this.plugin, "Data", "Saving data of " + record.effectiveId + " (" + record.tryCount + "/" + this.maxTries + ")");

        final var forceful = record.hitThreshold();
        try {
            database.saveData(record.playerData, record.reason);
        }

        // Any other error
        catch (Throwable throwable) {

            // Give up.
            if (forceful) {
                UtilityMethods.debug(this.plugin, "Data", "Error while saving " + record.effectiveId + ", giving up");
                throwable.printStackTrace();
                // TODO YML failsafe to avoid edge case data losses?
            }

            // Try again
            else {
                UtilityMethods.debug(this.plugin, "Data", "Error while saving " + record.effectiveId + ", next try in " + WAIT_TIME + "ms");
                this.enqueue(record.nextTry());
                return;
            }
        }

        ///////////////////////////////
        // Data is saved.
        ///////////////////////////////

        UtilityMethods.debug(this.plugin, "Data", "Saved data of " + record.effectiveId);

        // Data saved. Back to server thread.
        Tasks.runSync(plugin, () -> {
            if (record.reason != SessionUpdateReason.AUTOSAVE) record.playerData.markSessionClosed();
            record.future.complete(null);
        });
    }

    public class Record extends QueueRecord {
        public final SessionUpdateReason reason;

        public Record(@NotNull H playerData, @NotNull SessionUpdateReason reason) {
            this(playerData, playerData.getEffectiveId(), new CompletableFuture<>(), 0, 0, reason);
        }

        public Record(@NotNull H playerData,
                      @NotNull UUID effectiveId,
                      @NotNull CompletableFuture<Void> future,
                      long availableAt,
                      int tryCount,
                      SessionUpdateReason reason) {
            super(playerData, effectiveId, future, availableAt, tryCount);

            this.reason = reason;
        }

        @Override
        @NotNull
        public Record nextTry() {
            return new Record(this.playerData, this.effectiveId, this.future, System.currentTimeMillis() + WAIT_TIME, this.tryCount + 1, this.reason);
        }
    }
}
