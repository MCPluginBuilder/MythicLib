package io.lumine.mythic.lib.data;

import io.lumine.mythic.lib.data.queue.DataLoadResult;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import io.lumine.mythic.lib.util.Closeable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface Database<H extends SynchronizedDataHolder, O extends OfflineDataHolder> extends Closeable {

    @NotNull
    public MMOPlugin getPlugin();

    /**
     * Called once on server startup. This can be used for SQL support
     * to initialize database tables, and make sure they are up to date.
     */
    public void setup();

    /**
     * Retrieves all existing player UUIDs from the database.
     */
    public List<UUID> retrieveAllPlayerIds();

    /**
     * Called when player data must be saved in database.
     *
     * @param playerData Player data to save
     * @param reason     Reason of saving, behaviour might differ.
     * @implNote This method should be called async so there is no need
     *         to run async tasks inside of this method implementation.
     */
    public void saveData(@NotNull H playerData, @NotNull SessionUpdateReason reason) throws RuntimeException;

    /**
     * This method is always called ASYNC inside a newly created thread. It should
     * run the SQL methods or local config lookups in order to load player data.
     * <p>
     * The implementation of this method should be error safe, that is, if an exception
     * is returned, it will most likely result in ghost session errors.
     *
     * @param playerData Player data to be loaded
     * @return If loading was successful. If not, no event shall be called.
     */
    @NotNull
    public DataLoadResult loadData(@NotNull H playerData, boolean force) throws RuntimeException;

    public void confirmReception(@NotNull H playerData);

    public O getOffline(@NotNull UUID profileId);
}
