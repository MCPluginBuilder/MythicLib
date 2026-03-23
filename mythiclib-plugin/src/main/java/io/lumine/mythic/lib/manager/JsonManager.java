package io.lumine.mythic.lib.manager;

import com.google.gson.JsonElement;
import io.lumine.mythic.lib.MythicLib;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class JsonManager {

    @Deprecated
    @NotNull
    public <T> T parse(String s, Class<T> c) {
        return MythicLib.plugin.getGson().fromJson(s, c);
    }

    @Deprecated
    @NotNull
    public String toString(JsonElement json) {
        return MythicLib.plugin.getGson().toJson(json);
    }
}
