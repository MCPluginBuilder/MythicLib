package io.lumine.mythic.lib.data.queue;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.profile.ProfileSession;
import io.lumine.mythic.lib.profile.ProfileSessionState;
import io.lumine.mythic.lib.util.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DataLoadQueue<H extends SynchronizedDataHolder> extends DataQueue<H> {
    public DataLoadQueue(@NotNull SynchronizedDataManager<H, ?> manager) {
        super(manager);
    }

    @NotNull
    public CompletableFuture<Void> enqueue(@NotNull H playerData) {
        final var record = new Record(playerData);
        super.enqueue(record);
        return record.future;
    }

    @Override
    protected void processRecord(QueueRecord recordI) {
        final var record = (Record) recordI;

        // Invalidate check, player went offline/session closed
        if (record.invalidate()) return;

        ///////////////////////////////
        // Fetch data
        ///////////////////////////////

        UtilityMethods.debug(this.plugin, "Data", "Fetching data of " + record.effectiveId + " (" + record.tryCount + "/" + this.maxTries + "). Queued: " + this.recordQueue.size());

        // Try to load player data
        final var forceful = record.hitThreshold();
        DataLoadResult result;
        try {
            result = this.database.loadData(record.playerData, record.hitThreshold());
        }

        // Data found but not sync
        catch (DataNotReadyException exception) {
            UtilityMethods.debug(this.plugin, "Data", "Not sync data found for " + record.effectiveId + ", next try in " + WAIT_TIME + "ms");
            this.enqueue(record.nextTry());
            return;
        }

        // Any other error
        catch (Exception | LinkageError throwable) {

            // Give up.
            if (forceful) {
                UtilityMethods.debug(this.plugin, "Data", "Error while loading " + record.effectiveId + ", giving up");
                throwable.printStackTrace();
                result = new DataLoadResult(true, false);
            }

            // Try again
            else {
                UtilityMethods.debug(this.plugin, "Data", "Error while loading " + record.effectiveId + ", next try in " + WAIT_TIME + "ms");
                UtilityMethods.debug(this.plugin, "Data", "Error: " + throwable.getMessage());
                this.enqueue(record.nextTry());
                return;
            }
        }

        //////////////////////////////////
        // Data was loaded successfully
        //////////////////////////////////

        UtilityMethods.debug(this.plugin, "Data", "Data loaded (sync=" + result.isSync() + " empty=" + result.isEmpty() + ") for " + record.effectiveId);

        // Invalidate check
        if (record.invalidate()) return;

        if (result.isEmpty()) this.manager.loadEmptyPlayerData(record.playerData);
        final var isLookup = record.playerData.getMMOPlayerData().isLookup();
        if (!isLookup) this.database.confirmReception(record.playerData);

        ///////////////////////////////
        // Back to main thread
        ///////////////////////////////

        // Try scheduling task
        try {
            Tasks.runSync(plugin, () -> {

                // Player could go offline while transitioning to main thread
                if (record.invalidate()) return;

                if (!isLookup) {
                    record.playerData.markSessionReady(); // Mark as ready
                    Bukkit.getPluginManager().callEvent(new SynchronizedDataLoadEvent(manager, record.playerData));
                }
                record.future.complete(null); // Complete future
            });
        } catch (IllegalPluginAccessException exception) {
            // Plugin is disabled, complete future anyway
            record.future.complete(null);
        }
    }

    protected class Record extends QueueRecord {
        final @Nullable ProfileSession session;

        public Record(@NotNull H playerData) {
            this(playerData, playerData.getEffectiveId(), new CompletableFuture<>(), 0, 0, extractPlayerSession(playerData));
        }

        public Record(@NotNull H playerData,
                      @NotNull UUID effectiveId,
                      @NotNull CompletableFuture<Void> future,
                      long availableAt,
                      int tryCount,
                      @Nullable ProfileSession session) {
            super(playerData, effectiveId, future, availableAt, tryCount);

            this.session = session;
        }

        @NotNull
        @Override
        public Record nextTry() {
            return new Record(this.playerData, this.effectiveId, this.future, System.currentTimeMillis() + WAIT_TIME, this.tryCount + 1, this.session);
        }

        boolean invalidate() {

            // Lookup is never invalidated by definition
            if (playerData.getMMOPlayerData().isLookup()) return false;

            // This method should check if the player is offline.
            // The data session `alive` flag is set to false if the player logs out
            // or if for any reason the profile session closes
            final var invalidated = this.session == null ? !playerData.getMMOPlayerData().isOnline() : session.getState() != ProfileSessionState.OPENING;

            if (invalidated) {
                UtilityMethods.debug(DataLoadQueue.this.plugin, "Data", "Stopped data retrieval as " + effectiveId + " went offline");
                this.future.complete(null); // Complete future
            }

            return invalidated;
        }
    }

    @Nullable ProfileSession extractPlayerSession(H playerData) {
        return this.plugin.isProfilePlugin() || playerData.getMMOPlayerData().isLookup() ? null : playerData.getMMOPlayerData().getProfileSession();
    }
}
