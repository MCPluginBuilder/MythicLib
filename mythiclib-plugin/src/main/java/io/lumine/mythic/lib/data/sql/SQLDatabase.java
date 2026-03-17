package io.lumine.mythic.lib.data.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.lumine.mythic.lib.data.Database;
import io.lumine.mythic.lib.data.OfflineDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.queue.DataLoadResult;
import io.lumine.mythic.lib.data.queue.DataNotReadyException;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import io.lumine.mythic.lib.util.Tasks;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
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

public abstract class SQLDatabase<H extends SynchronizedDataHolder, O extends OfflineDataHolder> implements Database<H, O> {
    private final MMOPlugin plugin;
    private final HikariDataSource dataSource;
    protected final String userdataTableName, uuidFieldName;
    protected final String databaseName;

    public SQLDatabase(@NotNull MMOPlugin plugin, @NotNull String uuidFieldName) {
        this.plugin = plugin;
        final var config = hikariFromConfig(plugin);
        this.dataSource = new HikariDataSource(config.hikariConfig);

        this.databaseName = config.databaseName;
        this.userdataTableName = config.userdataTableName;
        this.uuidFieldName = uuidFieldName;
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void setup() {
        try {
            setupSQL();
        } catch (SQLException exception) {
            this.plugin.getLogger().log(Level.WARNING, "Got SQL exception during setup: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    public void close() {
        dataSource.close();
    }

    @NotNull
    public String getUserDataTableName() {
        return userdataTableName;
    }

    @NotNull
    public String getDatabaseName() {
        return databaseName;
    }

    @NotNull
    public MMOPlugin getPlugin() {
        return plugin;
    }

    //region Util methods

    @NotNull
    private static PreparedStatement prepareStatement(@NotNull Connection connection, @NotNull String sql, @NotNull String... params) throws SQLException {
        final var statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++)
            statement.setString(i + 1, params[i]);
        return statement;
    }

    public void executeQuery(@NotNull String sql, @NotNull SQLConsumer callback, @NotNull String... params) throws SQLException {
        try (var connection = getConnection();
             var statement = prepareStatement(connection, sql, params)) {
            callback.accept(statement.executeQuery());
        }
    }

    public void executeUpdate(@NotNull String sql, @NotNull String... params) throws SQLException {
        try (var connection = getConnection();
             var statement = prepareStatement(connection, sql, params)) {
            statement.executeUpdate();
        }
    }

    //endregion

    @Override
    public @NotNull DataLoadResult loadData(@NotNull H playerData, boolean force) {
        final var effectiveId = playerData.getEffectiveId();

        try (var connection = getConnection();
             var prepared = prepareStatement(connection, "SELECT * FROM `" + this.userdataTableName + "` WHERE `" + this.uuidFieldName + "` = ?;", effectiveId.toString());
             var resultSet = prepared.executeQuery()) {

            // Empty result set
            if (!resultSet.next()) {
                return new DataLoadResult(true, true);
            }

            // Load from result set
            else {
                final var isSaved = resultSet.getInt("is_saved") == 1;
                if (!isSaved && !force) throw new DataNotReadyException();
                return loadDataFromResultSet(playerData, resultSet, isSaved);
            }

        } catch (SQLException exception) {
            throw new RuntimeException("SQL exception: " + exception.getMessage(), exception);
        }
    }

    protected abstract void setupSQL() throws SQLException;

    @NotNull
    protected abstract DataLoadResult loadDataFromResultSet(@NotNull H playerData, @NotNull ResultSet result, boolean isSaved) throws SQLException;

    @Override
    public void saveData(@NotNull H playerData, @NotNull SessionUpdateReason saveReason) {
        var builder = new UpdateRequestBuilder<>(this);
        var effectiveId = playerData.getEffectiveId();

        // Append mandatory fields
        builder.appendString("uuid", effectiveId);
        builder.appendInt("is_saved", saveReason == SessionUpdateReason.AUTOSAVE ? 0 : 1);

        // Populate request
        setupSaveRequest(playerData, builder);

        // Execute
        builder.execute();
    }

    protected abstract void setupSaveRequest(@NotNull H playerData, @NotNull UpdateRequestBuilder<H> builder);

    @Override
    public void confirmReception(@NotNull H playerData) {
        final var effectiveId = playerData.getEffectiveId();

        try (var connection = getConnection();
             var prepared = prepareStatement(connection, "INSERT INTO `" + this.userdataTableName + "` (`" + this.uuidFieldName + "`, `is_saved`) VALUES(?, 0) ON DUPLICATE KEY UPDATE `is_saved` = 0;", effectiveId.toString())) {
            prepared.executeUpdate();
        } catch (Exception exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not confirm data sync of " + effectiveId);
            exception.printStackTrace();
        }
    }

    @Override
    public List<UUID> retrieveAllPlayerIds() {
        final List<UUID> uuids;

        try (var connection = getConnection();
             var prepared = connection.prepareStatement(String.format("SELECT `%s` FROM `%s`;", uuidFieldName, userdataTableName));
             var result = prepared.executeQuery()) {

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

    //region Read Hikari from YAML config

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_DATABASE = "minecraft";
    private static final String DEFAULT_SQLITE_PATH = "userdata.db";
    private static final int DEFAULT_PORT = 3306;
    private static final int DEFAULT_MAX_POOL_SIZE = 10;
    private static final int DEFAULT_MAX_LIFE_TIME = 300000;
    private static final int DEFAULT_CONNECTION_TIME_OUT = 10000;
    private static final int DEFAULT_LEAK_DETECT_THRESHOLD = 10000;

    @NotNull
    private static DatabaseConfig hikariFromConfig(@NotNull MMOPlugin plugin) {

        final var hikariConfig = new HikariConfig();
        final ConfigurationSection config;
        final String databaseName;
        ;

        // MySQL
        if (plugin.getConfig().getBoolean("mysql.enabled")) {
            config = plugin.getConfig().getConfigurationSection("mysql");
            final var host = config.getString("host", DEFAULT_HOST);
            final var port = config.getInt("port", DEFAULT_PORT);
            databaseName = config.getString("database", DEFAULT_DATABASE);

            hikariConfig.setPoolName("hikari-mysql-" + plugin.getName());
            hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", host, port, databaseName));
        }

        // SQLite
        else if (plugin.getConfig().getBoolean("sqlite.enabled")) {
            config = plugin.getConfig().getConfigurationSection("sqlite");
            databaseName = config.getString("database", DEFAULT_DATABASE);
            final var databasePath = config.getString("path", DEFAULT_SQLITE_PATH);
            final var url = String.format("jdbc:sqlite:%s", Path.of(plugin.getDataFolder().toString(), databasePath));

            plugin.getLogger().log(Level.INFO, "path= " + url);

            hikariConfig.setPoolName("hikari-sqlite-" + plugin.getName());
            hikariConfig.setJdbcUrl(url);
        }

        // Wth?
        else throw new IllegalArgumentException("No SQL option enabled");

        final String userdataTableName = config.getString("userdata-table-name");

        hikariConfig.setUsername(config.getString("user", DEFAULT_USERNAME));
        hikariConfig.setPassword(config.getString("pass", DEFAULT_PASSWORD));
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setMaximumPoolSize(config.getInt("maxPoolSize", DEFAULT_MAX_POOL_SIZE));
        hikariConfig.setMaxLifetime(config.getLong("maxLifeTime", DEFAULT_MAX_LIFE_TIME));
        hikariConfig.setConnectionTimeout(config.getLong("connectionTimeOut", DEFAULT_CONNECTION_TIME_OUT));
        hikariConfig.setLeakDetectionThreshold(config.getLong("leakDetectionThreshold", DEFAULT_LEAK_DETECT_THRESHOLD));
        if (config.isConfigurationSection("properties"))
            for (String s : config.getConfigurationSection("properties").getKeys(false))
                hikariConfig.addDataSourceProperty(s, config.getString("properties." + s));

        return new DatabaseConfig(hikariConfig, databaseName, userdataTableName);
    }

    private static class DatabaseConfig {
        public final HikariConfig hikariConfig;
        public final String databaseName, userdataTableName;

        public DatabaseConfig(HikariConfig hikariConfig, String databaseName, String userdataTableName) {
            this.hikariConfig = hikariConfig;
            this.databaseName = databaseName;
            this.userdataTableName = userdataTableName;
        }
    }

    //endregion

    //region Deprecated methods

    @Deprecated
    @NotNull
    public CompletableFuture<Void> executeQueryAsync(@NotNull String sql, @NotNull Consumer<ResultSet> callback, @NotNull String... params) {
        return Tasks.runAsync(plugin, () -> {
            try {
                executeQuery(sql, callback::accept, params);
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.WARNING, "SQL Error (async): " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Deprecated
    @NotNull
    public CompletableFuture<Void> executeUpdateAsync(@NotNull String sql, String... params) {
        return Tasks.runAsync(plugin, () -> {
            try {
                executeUpdate(sql, params);
            } catch (SQLException e) {
                this.plugin.getLogger().log(Level.WARNING, "SQL Error (async): " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Deprecated
    public void getResult(@NotNull String sql, @NotNull Consumer<ResultSet> supplier) {
        try {
            executeQuery(sql, supplier::accept);
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @NotNull
    @Deprecated
    public CompletableFuture<Void> getResultAsync(String sql, Consumer<ResultSet> supplier) {
        return Tasks.runAsync(plugin, () -> getResult(sql, supplier));
    }

    @Deprecated
    public void execute(Consumer<Connection> execute) {
        try (Connection connection = getConnection()) {
            execute.accept(connection);
        } catch (Exception throwable) {
            this.plugin.getLogger().log(Level.WARNING, "SQL Error: " + throwable.getMessage());
            throwable.printStackTrace();
        }
    }

    @NotNull
    @Deprecated
    public CompletableFuture<Void> executeAsync(Consumer<Connection> execute) {
        return Tasks.runAsync(plugin, () -> {
            try (Connection connection = getConnection()) {
                execute.accept(connection);
            } catch (Exception throwable) {
                this.plugin.getLogger().log(Level.WARNING, "SQL Error (async): " + throwable.getMessage());
                throwable.printStackTrace();
            }
        });
    }

    //endregion
}

