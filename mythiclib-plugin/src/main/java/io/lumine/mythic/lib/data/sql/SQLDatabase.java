package io.lumine.mythic.lib.data.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.data.DataLoadResult;
import io.lumine.mythic.lib.data.Database;
import io.lumine.mythic.lib.data.OfflineDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.util.Tasks;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

// TODO clear methods and implement CompletableFutures
public abstract class SQLDatabase<H extends SynchronizedDataHolder, O extends OfflineDataHolder> implements Database<H, O> {
    private final MMOPlugin plugin;
    private final HikariDataSource dataSource;
    private final String userdataTableName, uuidFieldName;

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_DATABASE = "minecraft";
    private static final int DEFAULT_PORT = 3306;

    public SQLDatabase(@NotNull MMOPlugin plugin, @NotNull String userdataTableName, @NotNull String uuidFieldName) {
        this.plugin = plugin;
        this.userdataTableName = userdataTableName;
        this.uuidFieldName = uuidFieldName;

        // Prepare Hikari config
        final ConfigurationSection config = plugin.getConfig().getConfigurationSection("mysql");
        final HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("hikari-" + plugin.getName());
        hikari.setJdbcUrl("jdbc:mysql://" + config.getString("host", DEFAULT_HOST) + ":" + config.getInt("port", DEFAULT_PORT) + "/" + config.getString("database", DEFAULT_DATABASE));
        hikari.setUsername(config.getString("user", DEFAULT_USERNAME));
        hikari.setPassword(config.getString("pass", DEFAULT_PASSWORD));
        hikari.setMaximumPoolSize(config.getInt("maxPoolSize", 10));
        hikari.setMaxLifetime(config.getLong("maxLifeTime", 300000));
        hikari.setConnectionTimeout(config.getLong("connectionTimeOut", 10000));
        hikari.setLeakDetectionThreshold(config.getLong("leakDetectionThreshold", 150000));
        if (config.isConfigurationSection("properties"))
            for (String s : config.getConfigurationSection("properties").getKeys(false))
                hikari.addDataSourceProperty(s, config.getString("properties." + s));

        dataSource = new HikariDataSource(hikari);
    }

    @Override
    public boolean refreshConnection() {
        // Connection pool
        return true;
    }

    @NotNull
    public MMOPlugin getPlugin() {
        return plugin;
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) dataSource.close();
    }

    //region Util methods

    public void executeQuery(@NotNull String sql, @NotNull Consumer<ResultSet> callback, @NotNull String... params) {
        try (Connection connection = getConnection()) {
            final var statement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++)
                statement.setString(i + 1, params[i]);
            callback.accept(statement.executeQuery());
        } catch (Throwable throwable) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not open SQL result statement:");
            throwable.printStackTrace();
        }
    }

    public void executeUpdate(@NotNull String sql, @NotNull String... params) {
        try (Connection connection = getConnection()) {
            final var statement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++)
                statement.setString(i + 1, params[i]);
            statement.executeUpdate();
        } catch (Throwable throwable) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "Could not execute SQL update: " + throwable.getMessage());
            throwable.printStackTrace();
        }
    }

    @Deprecated
    public void getResult(@NotNull String sql, @NotNull Consumer<ResultSet> supplier) {
        this.executeQuery(sql, supplier);
    }

    @NotNull
    @Deprecated
    public CompletableFuture<Void> getResultAsync(String sql, Consumer<ResultSet> supplier) {
        return Tasks.runAsync(plugin, () -> getResult(sql, supplier));
    }

    @NotNull
    @Deprecated
    public CompletableFuture<Void> executeUpdateAsync(String sql) {
        return Tasks.runAsync(plugin, () -> executeUpdate(sql));
    }

    /**
     * Retrieve a connection from pool and prepare it for
     * use. Connection is closed when consumer is called.
     *
     * @param execute Action to be done with connection
     * @deprecated Use try-with-resources with {@link #getConnection()} instead
     */
    @Deprecated
    public void execute(Consumer<Connection> execute) {
        try (Connection connection = getConnection()) {
            execute.accept(connection);
        } catch (Throwable throwable) {
            MythicLib.plugin.getLogger().log(Level.WARNING, "SQL error: " + throwable.getMessage());
            throwable.printStackTrace();
        }
    }

    /**
     * Retrieve a connection from pool and prepare it for
     * use. Connection is closed when consumer is called.
     * <p>
     * Called asynchronously.
     *
     * @param execute Action to be done with connection
     */
    @NotNull
    @Deprecated
    public CompletableFuture<Void> executeAsync(Consumer<Connection> execute) {
        return Tasks.runAsync(plugin, () -> {
            try (Connection connection = getConnection()) {
                execute.accept(connection);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    //endregion

    @Override
    public @NotNull DataLoadResult loadData(@NotNull H playerData, boolean force) {
        final var effectiveId = playerData.getEffectiveId();

        DataLoadResult returnValue;

        try (Connection connection = getConnection()) {
            PreparedStatement prepared = connection.prepareStatement("SELECT * FROM `" + this.userdataTableName + "` WHERE `" + this.uuidFieldName + "` = ?;");
            prepared.setString(1, effectiveId.toString());

            UtilityMethods.debug(this.plugin, "SQL", "Trying to load data of " + effectiveId);
            ResultSet resultSet = prepared.executeQuery(); // Freezes thread

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
            this.plugin.getLogger().log(Level.WARNING, "Got SQL exception " + playerData.getEffectiveId() + ": " + exception.getMessage());
            exception.printStackTrace(); // TODO remove
            returnValue = new DataLoadResult(DataLoadResult.Type.FAILURE);
        } catch (Throwable throwable) {
            // Real plugin exceptions.
            this.plugin.getLogger().log(Level.WARNING, "Could not load data of " + playerData.getEffectiveId() + ": " + throwable.getMessage());
            throwable.printStackTrace();
            returnValue = new DataLoadResult(DataLoadResult.Type.FAILURE);
        }

        return returnValue;
    }

    @NotNull
    protected abstract DataLoadResult loadDataFromResultSet(@NotNull H playerData, @NotNull ResultSet result, boolean isSaved) throws SQLException;

    @Override
    public void confirmReception(@NotNull H playerData) {
        final var effectiveId = playerData.getEffectiveId();

        try (Connection connection = getConnection()) {
            PreparedStatement prepared = connection.prepareStatement("INSERT INTO `" + this.userdataTableName + "` (`" + this.uuidFieldName + "`, `is_saved`) VALUES(?, 0) ON DUPLICATE KEY UPDATE `is_saved` = 0;");
            prepared.setString(1, effectiveId.toString());
            prepared.executeUpdate();
        } catch (Throwable exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not confirm data sync of " + effectiveId);
            exception.printStackTrace();
        }
    }

    @Override
    public List<UUID> retrieveAllPlayerIds() {
        final List<UUID> uuids;

        try (Connection connection = getConnection()) {
            PreparedStatement prepared = connection.prepareStatement(String.format("SELECT `%s` FROM `%s`;", uuidFieldName, userdataTableName));
            ResultSet result = prepared.executeQuery(); // Freezes thread

            uuids = new ArrayList<>();
            while (result.next()) {
                var uuid = UUID.fromString(result.getString(uuidFieldName));
                uuids.add(uuid);
            }

        } catch (Exception throwable) {
            throw new RuntimeException("Could not retrieve player UUIDs", throwable);
        }

        return uuids;
    }
}

