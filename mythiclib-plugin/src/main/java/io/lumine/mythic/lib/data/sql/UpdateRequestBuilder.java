package io.lumine.mythic.lib.data.sql;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UpdateRequestBuilder<H extends SynchronizedDataHolder> {
    private final SQLDatabase<H, ?> database;

    private boolean executed;

    /**
     * Strings to be injected
     */
    private final List<Entry> entries = new ArrayList<>();

    public UpdateRequestBuilder(@NotNull SQLDatabase<H, ?> database) {
        this.database = database;
    }

    //region Append

    public void appendString(String key, Object toString) {
        append(key, DataType.STRING, Objects.toString(toString));
    }

    public void appendInt(String key, int value) {
        append(key, DataType.INTEGER, value);
    }

    public void appendLong(String key, long value) {
        append(key, DataType.LONG, value);
    }

    public void appendDouble(String key, double value) {
        append(key, DataType.DOUBLE, value);
    }

    /**
     * Appends an object, stored as a JsonObject in string format.
     */
    public void appendCollection(String key, Iterable<?> collection) {
        var json = new JsonArray();
        for (var object : collection) json.add(String.valueOf(object));
        appendString(key, json.toString());
    }

    /**
     * Appends an object, stored as a JsonObject in string format.
     */
    public void appendObject(String key, Map<?, ?> collection) {
        var json = new JsonObject();
        for (var entry : collection.entrySet()) json.addProperty(entry.getKey().toString(), entry.getValue().toString());

        appendString(key, json.toString());
    }

    private void append(@NotNull String key, @NotNull DataType dataType, @NotNull Object value) {
        Objects.requireNonNull(value, "Value cannot be null");
        this.entries.add(new Entry(value, key, dataType));
    }

    //endregion

    private String buildRequest() {
        var empty = true;

        var onDuplicates = new StringBuilder();
        var valueKeys = new StringBuilder();
        var valuePlaceholders = new StringBuilder();

        for (var entry : entries) {

            // Append commas
            if (empty) empty = false;
            else {
                onDuplicates.append(",");
                valueKeys.append(",");
                valuePlaceholders.append(",");
            }

            // Append keys or ? placeholders
            onDuplicates.append('`').append(entry.key).append("`=VALUES(`").append(entry.key).append("`)");
            valueKeys.append('`').append(entry.key).append('`');
            valuePlaceholders.append('?');
        }

        // Build final request
        return "INSERT INTO " + database.getUserDataTableName() +
                " (" + valueKeys + ") " +
                "VALUES (" + valuePlaceholders + ") ON DUPLICATE KEY UPDATE " + onDuplicates + ';';
    }

    private void prepareStatement(PreparedStatement statement) throws SQLException {
        var index = 1;
        for (var entry : this.entries) {
            entry.dataType.handler.apply(statement, index, entry.injected);
            index++;
        }
    }

    public void execute() {
        if (executed) throw new IllegalStateException("Already executed");
        executed = true;

        var request = buildRequest();

        try (var connection = database.getConnection(); var statement = connection.prepareStatement(request)) {

            // Prepare statement
            this.prepareStatement(statement);

            // Execute statement
            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Could not save player data", exception);
        }
    }

    @FunctionalInterface
    private interface DataTypeHandler {
        void apply(PreparedStatement statement, int index, Object object) throws SQLException;
    }

    private enum DataType {
        INTEGER((statement, index, object) -> statement.setInt(index, (int) object)),
        LONG((statement, index, object) -> statement.setLong(index, (long) object)),
        DOUBLE((statement, index, object) -> statement.setDouble(index, (double) object)),
        FLOAT(((statement, index, object) -> statement.setFloat(index, (float) object))),
        STRING(((statement, index, object) -> statement.setString(index, object.toString()))),
        ;

        final DataTypeHandler handler;

        DataType(DataTypeHandler handler) {
            this.handler = handler;
        }
    }

    private static class Entry {
        final Object injected;
        final String key;
        final DataType dataType;

        public Entry(Object injected, String key, DataType dataType) {
            this.injected = injected;
            this.key = key;
            this.dataType = dataType;
        }
    }
}
