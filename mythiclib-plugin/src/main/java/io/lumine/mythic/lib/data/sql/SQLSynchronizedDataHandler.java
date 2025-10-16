package io.lumine.mythic.lib.data.sql;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.data.DataLoadResult;
import io.lumine.mythic.lib.data.OfflineDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataHandler;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class SQLSynchronizedDataHandler<H extends SynchronizedDataHolder, O extends OfflineDataHolder> implements SynchronizedDataHandler<H, O> {
    private final SQLDataSource dataSource;
    private final String userdataTableName, uuidFieldName;

    public SQLSynchronizedDataHandler(@NotNull SQLDataSource dataSource, @NotNull String userdataTableName, @NotNull String uuidFieldName) {
        this.dataSource = dataSource;
        this.userdataTableName = userdataTableName;
        this.uuidFieldName = uuidFieldName;
    }

    public SQLDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public @NotNull DataLoadResult loadData(@NotNull H playerData, boolean force) {
        final var effectiveId = playerData.getEffectiveId();

        DataLoadResult returnValue;

        Connection connection = null;
        PreparedStatement prepared = null;
        ResultSet resultSet = null;

        try {

            connection = dataSource.getConnection();
            prepared = connection.prepareStatement("SELECT * FROM `" + this.userdataTableName + "` WHERE `" + this.uuidFieldName + "` = ?;");
            prepared.setString(1, effectiveId.toString());

            UtilityMethods.debug(dataSource.getPlugin(), "SQL", "Trying to load data of " + effectiveId);
            resultSet = prepared.executeQuery(); // Freezes thread

            // Empty result set
            if (!resultSet.next()) {
                returnValue = new DataLoadResult(DataLoadResult.Type.SUCCESS, true, true);
            }

            // Load from result set
            else {
                final var isSaved = resultSet.getInt("is_saved") == 1;

                returnValue = force || isSaved
                        ? loadDataFromResultSet(playerData, resultSet, isSaved)
                        : new DataLoadResult(DataLoadResult.Type.NOT_SYNC, false, isSaved);
            }

        } catch (SQLException exception) {
            dataSource.getPlugin().getLogger().log(Level.WARNING, "Got SQL exception " + playerData.getEffectiveId() + ": " + exception.getMessage());
            exception.printStackTrace(); // TODO remove
            returnValue = new DataLoadResult(DataLoadResult.Type.FAILURE);
        } catch (Throwable throwable) {
            // Real plugin exceptions.
            dataSource.getPlugin().getLogger().log(Level.WARNING, "Could not load data of " + playerData.getEffectiveId() + ": " + throwable.getMessage());
            throwable.printStackTrace();
            returnValue = new DataLoadResult(DataLoadResult.Type.FAILURE);
        } finally {
            closeSqlResources(connection, prepared, resultSet);
        }

        return returnValue;
    }

    @NotNull
    protected abstract DataLoadResult loadDataFromResultSet(@NotNull H playerData, @NotNull ResultSet result, boolean isSaved) throws SQLException;

    @Override
    public void confirmReception(@NotNull H playerData) {
        final var effectiveId = playerData.getEffectiveId();

        Connection connection = null;
        PreparedStatement prepared = null;

        try {
            connection = dataSource.getConnection();

            prepared = connection.prepareStatement("INSERT INTO `" + this.userdataTableName + "` (`" + this.uuidFieldName + "`, `is_saved`) VALUES(?, 0) ON DUPLICATE KEY UPDATE `is_saved` = 0;");
            prepared.setString(1, effectiveId.toString());
            prepared.executeUpdate();
        } catch (Throwable exception) {
            dataSource.getPlugin().getLogger().log(Level.WARNING, "Could not confirm data sync of " + effectiveId);
            exception.printStackTrace();
        } finally {
            closeSqlResources(connection, prepared, null);
        }
    }

    /**
     * Util method to close resources. If any of the parameters is null,
     * it is ignored and MythicLib will not try to close it
     *
     * @param connection SQL connection
     * @param prepared   Prepared statement
     * @param result     SQL statement result
     */
    protected void closeSqlResources(@Nullable Connection connection, @Nullable PreparedStatement prepared, @Nullable ResultSet result) {

        if (connection != null) try {
            connection.close();
        } catch (SQLException exception) {
            dataSource.getPlugin().getLogger().log(Level.WARNING, "Could not close connection: " + exception.getMessage());
        }

        if (prepared != null) try {
            prepared.close();
        } catch (SQLException exception) {
            dataSource.getPlugin().getLogger().log(Level.WARNING, "Could not close prepared SQL statement: " + exception.getMessage());
        }

        if (result != null) try {
            result.close();
        } catch (SQLException exception) {
            dataSource.getPlugin().getLogger().log(Level.WARNING, "Could not close SQL result set: " + exception.getMessage());
        }
    }

    @Override
    public void close() {
        getDataSource().close();
    }

    @Override
    public List<UUID> retrieveAllPlayerIds() {

        // Fields that must be closed afterward
        @Nullable Connection connection = null;
        @Nullable PreparedStatement prepared = null;
        @Nullable ResultSet result = null;
        final List<UUID> uuids;

        try {
            connection = dataSource.getConnection();
            prepared = connection.prepareStatement(String.format("SELECT `%s` FROM `%s`;", uuidFieldName, userdataTableName));
            result = prepared.executeQuery(); // Freezes thread

            uuids = new ArrayList<>();
            while (result.next()) {
                var uuid = UUID.fromString(result.getString(uuidFieldName));
                uuids.add(uuid);
            }

        } catch (Exception throwable) {
            throw new RuntimeException("Could not retrieve player UUIDs", throwable);
        } finally {

            // Close resources
            try {
                if (result != null) result.close();
                if (prepared != null) prepared.close();
                if (connection != null) connection.close();
            } catch (SQLException exception) {
                throw new RuntimeException("Could not close SQL resources", exception);
            }
        }

        return uuids;
    }
}
