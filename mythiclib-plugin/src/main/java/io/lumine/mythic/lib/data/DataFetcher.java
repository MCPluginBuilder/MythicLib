package io.lumine.mythic.lib.data;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.profile.ProfileSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TODO write docs
 */
public class DataFetcher<H extends SynchronizedDataHolder, O extends OfflineDataHolder> {
    private final SynchronizedDataManager<H, O> manager;
    private final Database<H, O> database;
    private final MMOPlugin plugin;
    private final H playerData;
    private final UUID effectiveId;
    //private final long startTime = System.currentTimeMillis();
    private final int maxTries;

    private int tryCount;

    @Nullable
    private final ProfileSession profileSession;

    private static final int WAIT_TIME = 1000;

    /**
     * TODO write docs
     */
    public DataFetcher(@NotNull SynchronizedDataManager<H, O> manager, @NotNull H playerData) {
        this.playerData = playerData;
        this.database = manager.getDatabase();
        this.manager = manager;
        this.plugin = manager.getOwningPlugin();
        this.effectiveId = playerData.getEffectiveId();
        this.profileSession = plugin.isProfilePlugin() || playerData.getMMOPlayerData().isLookup() ? null : playerData.getMMOPlayerData().getProfileSession();
        maxTries = MythicLib.plugin.getMMOConfig().maxSyncTries;
    }

    @NotNull
    public H getData() {
        return playerData;
    }

    @NotNull
    public MMOPlugin getPlugin() {
        return plugin;
    }

    /**
     * Tries to fetch data once. If the maximum amount of fetches
     * hasn't been reached yet, it will try again later if no up-to-date
     * data has been retrieved.
     * <p>
     * This method freezes the thread and shall be called async.
     */
    @NotNull
    public DataLoadResult run() {

        while (true) {
            UtilityMethods.debug(this.plugin, "Data", "Fetching data of " + effectiveId);

            // Invalidate check
            if (checkInvalidate()) return new DataLoadResult(DataLoadResult.Type.OFFLINE_PLAYER);

            // Try to load player data
            final var force = this.tryCount >= this.maxTries;
            final var result = this.database.loadData(this.playerData, force);

            switch (result.type) {

                // Player went offline, just quit
                case OFFLINE_PLAYER:
                    UtilityMethods.debug(this.plugin, "Data", "Stopped data retrieval as '" + effectiveId + "' went offline");
                    return result;

                // Data found but not sync
                case NOT_SYNC:
                    UtilityMethods.debug(this.plugin, "Data", "Not sync data found for '" + effectiveId + "', next try in " + WAIT_TIME + "ms");
                    waitUntilNextTry();
                    tryCount++;
                    break;

                // Failure
                case FAILURE:
                    UtilityMethods.debug(this.plugin, "Data", "Error when loading '" + effectiveId + "', next try in " + WAIT_TIME + "ms");
                    waitUntilNextTry();
                    tryCount++;
                    break;

                // Tempo, keep on working
                case TEMPO:
                    UtilityMethods.debug(this.plugin, "Data", "Got tempo for '" + effectiveId + "', next try in " + WAIT_TIME + "ms");
                    waitUntilNextTry();
                    break;

                case SUCCESS:
                    UtilityMethods.debug(this.plugin, "Data", "Data fetch success sync=" + result.sync + " empty=" + result.empty + " for '" + effectiveId + "'");

                    // Invalidate check
                    if (checkInvalidate()) return new DataLoadResult(DataLoadResult.Type.OFFLINE_PLAYER);

                    if (result.empty) this.manager.loadEmptyPlayerData(this.playerData);
                    if (!playerData.getMMOPlayerData().isLookup()) // TODO call not safe!!!
                        this.database.confirmReception(playerData);

                    return result;

                // Wtf?
                default:
                    throw new IllegalStateException("Unhandled data fetch result");
            }
        }
    }

    private void waitUntilNextTry() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    private boolean checkInvalidate() {

        // Lookup is never invalidated by definition
        if (playerData.getMMOPlayerData().isLookup()) return false;

        // This method should check if the player is offline.
        // The data session `alive` flag is set to false if the player logs out
        // or if for any reason the profile session closes
        final var invalidated = profileSession == null ? !playerData.getMMOPlayerData().isOnline() : profileSession.isDead();

        if (invalidated) {
            UtilityMethods.debug(this.plugin, "SQL", "Stopped data retrieval as '" + effectiveId + "' went offline");
        }

        return invalidated;
    }
}
