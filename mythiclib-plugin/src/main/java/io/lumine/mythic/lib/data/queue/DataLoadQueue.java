package io.lumine.mythic.lib.data.queue;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.SynchronizedDataLoadEvent;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.profile.ProfileSession;
import io.lumine.mythic.lib.util.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DataLoadQueue<H extends SynchronizedDataHolder> extends DataQueue<H> {
    private final int maxTries;


    public DataLoadQueue(@NotNull SynchronizedDataManager<H, ?> manager) {
        super(manager);

        maxTries = MythicLib.plugin.getMMOConfig().maxSyncTries;
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

        // Invalidate check, player went offline/session closed
        if (record.invalidate()) return;

        ///////////////////////////////
        // Fetch data
        ///////////////////////////////

        UtilityMethods.debug(this.plugin, "Data", "Fetching data of " + record.effectiveId);

        // Try to load player data
        final var force = record.tryCount >= this.maxTries;
        final var result = this.database.loadData(record.playerData, force);

        switch (result.type) {

            // Data found but not sync
            case NOT_SYNC:
                UtilityMethods.debug(this.plugin, "Data", "Not sync data found for '" + record.effectiveId + "', next try in " + WAIT_TIME + "ms");
                this.enqueue(record.nextTry(1));
                return;

            // Failure
            case FAILURE:
                UtilityMethods.debug(this.plugin, "Data", "Error when loading '" + record.effectiveId + "', next try in " + WAIT_TIME + "ms");
                this.enqueue(record.nextTry(1));
                break;

            // Tempo, keep on working
            case TEMPO:
                UtilityMethods.debug(this.plugin, "Data", "Got tempo for '" + record.effectiveId + "', next try in " + WAIT_TIME + "ms");
                this.enqueue(record.nextTry(0));
                break;

            case SUCCESS:
                UtilityMethods.debug(this.plugin, "Data", "Data fetch success sync=" + result.sync + " empty=" + result.empty + " for '" + record.effectiveId + "'");

                // Invalidate check
                if (record.invalidate()) return;

                if (result.empty) this.manager.loadEmptyPlayerData(record.playerData);
                if (!record.playerData.getMMOPlayerData().isLookup()) // TODO call not safe!!!
                    this.database.confirmReception(record.playerData);
                final var lookup = record.playerData.getMMOPlayerData().isLookup();

                ///////////////////////////////
                // Data is loaded, back to server thread
                ///////////////////////////////

                Tasks.runSync(plugin, () -> {

                    // Player could go offline while transitioning to main thread
                    if (record.invalidate()) return;

                    if (!lookup) {
                        record.playerData.markSessionReady(); // Mark as ready
                        Bukkit.getPluginManager().callEvent(new SynchronizedDataLoadEvent(manager, record.playerData, record.reason));
                    }
                    record.future.complete(null); // Complete future
                });

                return;

            // Wtf?
            default:
                throw new IllegalStateException("Unhandled data fetch result");
        }
    }

    protected class Record extends QueueRecord {
        final @Nullable Event reason;
        final int tryCount;
        final @Nullable ProfileSession session;

        public Record(@NotNull H playerData, @Nullable Event reason) {
            this(playerData, playerData.getEffectiveId(), new CompletableFuture<>(), 0, extractPlayerSession(playerData), reason, 0);
        }

        public Record(@NotNull H playerData,
                      @NotNull UUID effectiveId,
                      @NotNull CompletableFuture<Void> future,
                      long availableAt,
                      @Nullable ProfileSession session,
                      @Nullable Event reason,
                      int tryCount) {
            super(playerData, effectiveId, future, availableAt);

            this.session = session;
            this.reason = reason;
            this.tryCount = tryCount;
        }

        Record nextTry(int extraTry) {
            return new Record(this.playerData, this.effectiveId, this.future, System.currentTimeMillis() + WAIT_TIME, this.session, this.reason, this.tryCount + extraTry);
        }

        boolean invalidate() {

            // Lookup is never invalidated by definition
            if (playerData.getMMOPlayerData().isLookup()) return false;

            // This method should check if the player is offline.
            // The data session `alive` flag is set to false if the player logs out
            // or if for any reason the profile session closes
            final var invalidated = this.session == null ? !playerData.getMMOPlayerData().isOnline() : session.isDead();

            if (invalidated) {
                this.future.complete(null); // Complete future
                UtilityMethods.debug(DataLoadQueue.this.plugin, "SQL", "Stopped data retrieval as '" + effectiveId + "' went offline");
            }

            return invalidated;
        }
    }

    @Nullable ProfileSession extractPlayerSession(H playerData) {
        return this.plugin.isProfilePlugin() || playerData.getMMOPlayerData().isLookup() ? null : playerData.getMMOPlayerData().getProfileSession();
    }
}
