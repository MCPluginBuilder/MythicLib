package io.lumine.mythic.lib.util.configobject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class JsonWrapper implements ConfigObject {
    protected final String key;
    protected final JsonObject object;

    public JsonWrapper(String key, JsonObject object) {
        this.key = key;
        this.object = object;
    }

    protected JsonWrapper(String value) {

        /*
         * If there is no config, no need to parse the Json object.
         * Split, define key and find arguments
         */
        if (!value.contains("{") || !value.contains("}")) {
            object = new JsonObject();
            key = null;
            return;
        }

        // Load Json object
        final int begin = value.indexOf("{"), end = value.lastIndexOf("}") + 1;
        object = JsonParser.parseString(value.substring(begin, end)).getAsJsonObject();
        key = nullify(value.substring(0, begin));
    }

    @Nullable
    private String nullify(@Nullable String str) {
        return str == null || str.isEmpty() ? null : str;
    }

    //region Getters

    @Override
    public @NotNull String getString(@NotNull String key) {
        final var obj = this.object.get(key);
        if (obj == null) throw new MissingArgumentException(key);
        return obj.getAsString();
    }

    @Override
    public @Nullable String getString(@NotNull String key, @Nullable String defaultValue) {
        final var obj = object.get(key);
        return obj != null ? obj.getAsString() : defaultValue;
    }

    @Override
    public double getDouble(@NotNull String key) {
        final var obj = this.object.get(key);
        if (obj == null) throw new MissingArgumentException(key);
        return obj.getAsDouble();
    }

    @Override
    public double getDouble(@NotNull String key, double defaultValue) {
        final var obj = object.get(key);
        return obj != null ? obj.getAsDouble() : defaultValue;
    }

    @Override
    public int getInt(@NotNull String key) {
        final var obj = this.object.get(key);
        if (obj == null) throw new MissingArgumentException(key);
        return obj.getAsInt();
    }

    @Override
    public int getInt(@NotNull String key, int defaultValue) {
        final var obj = object.get(key);
        return obj != null ? obj.getAsInt() : defaultValue;
    }

    @Override
    public float getFloat(@NotNull String key) {
        final var obj = this.object.get(key);
        if (obj == null) throw new MissingArgumentException(key);
        return obj.getAsFloat();
    }

    @Override
    public float getFloat(@NotNull String key, float defaultValue) {
        final var obj = object.get(key);
        return obj == null ? defaultValue : obj.getAsFloat();
    }

    @Override
    public boolean getBoolean(@NotNull String key) {
        return object.get(key).getAsBoolean();
    }

    @Override
    public boolean getBoolean(@NotNull String key, boolean defaultValue) {
        return object.has(key) ? getBoolean(key) : defaultValue;
    }

    //endregion

    //region Finders

    @Override
    public String string(@NotNull String... aliases) {
        for (var alias : aliases)
            if (object.has(alias)) return object.get(alias).getAsString();
        throw new MissingArgumentException(aliases);
    }

    @Override
    public String stringFb(@NotNull String defaultValue, @NotNull String... aliases) {
        for (var alias : aliases)
            if (object.has(alias)) return object.get(alias).getAsString();
        return defaultValue;
    }

    @Override
    public int integer(@NotNull String... aliases) {
        for (var alias : aliases)
            if (object.has(alias)) return object.get(alias).getAsInt();
        throw new MissingArgumentException(aliases);
    }

    @Override
    public int integer(int defaultValue, @NotNull String... aliases) {
        for (var alias : aliases)
            if (object.has(alias)) return object.get(alias).getAsInt();
        return defaultValue;
    }

    @Override
    public double dble(@NotNull String... aliases) {
        for (var alias : aliases)
            if (object.has(alias)) return object.get(alias).getAsDouble();
        throw new MissingArgumentException(aliases);
    }

    @Override
    public double dble(double defaultValue, @NotNull String... aliases) {
        for (var alias : aliases)
            if (object.has(alias)) return object.get(alias).getAsDouble();
        return defaultValue;
    }

    @Override
    public float flpt(@NotNull String... aliases) {
        for (var alias : aliases)
            if (object.has(alias)) return object.get(alias).getAsFloat();
        throw new MissingArgumentException(aliases);
    }

    @Override
    public float flpt(float defaultValue, @NotNull String... aliases) {
        for (var alias : aliases)
            if (object.has(alias)) return object.get(alias).getAsFloat();
        return defaultValue;
    }

    @Override
    public boolean bool(@NotNull String... aliases) {
        for (var alias : aliases)
            if (object.has(alias)) return object.get(alias).getAsBoolean();
        throw new MissingArgumentException(aliases);
    }

    @Override
    public boolean bool(boolean defaultValue, @NotNull String... aliases) {
        for (var alias : aliases)
            if (object.has(alias)) return object.get(alias).getAsBoolean();
        return defaultValue;
    }

    //endregion

    @NotNull
    @Override
    public ConfigObject adaptObject(String key) {
        final JsonElement found = object.get(key);
        if (found == null) throw new MissingArgumentException(key);

        final JsonObject loadFrom;
        if (found instanceof JsonObject) loadFrom = found.getAsJsonObject();
        else if (found instanceof JsonPrimitive) {
            loadFrom = new JsonObject();
            loadFrom.addProperty("type", found.getAsString());
        } else throw new IllegalArgumentException("Expecting either a string or object");

        return new JsonWrapper(key, loadFrom);
    }

    @NotNull
    @Override
    public ConfigObject getObject(String key) {
        return new JsonWrapper(key, object.getAsJsonObject(key));
    }

    @Override
    public boolean contains(String key) {
        return object.has(key);
    }

    @NotNull
    @Override
    public Set<String> getKeys() {
        return object.keySet();
    }

    @Override
    public String getKey() {
        return key;
    }
}
