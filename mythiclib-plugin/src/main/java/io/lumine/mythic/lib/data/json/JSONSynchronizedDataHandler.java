package io.lumine.mythic.lib.data.json;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.data.*;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.Jsonable;
import io.lumine.mythic.lib.util.config.JsonFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class JSONSynchronizedDataHandler<H extends SynchronizedDataHolder & Jsonable, O extends OfflineDataHolder> implements SynchronizedDataHandler<H, O> {
    private final MMOPlugin owning;

    public JSONSynchronizedDataHandler(MMOPlugin owning) {
        this.owning = Objects.requireNonNull(owning, "Plugin cannot be null");
    }

    @Override
    public void saveData(@NotNull H playerData, @NotNull SaveReason reason) {
        // TODO json object is uselessly loaded into memory
        final JsonFile file = getUserFile(playerData);
        file.setContent(playerData.toJson());
        file.save();
    }

    @NotNull
    @Override
    public DataLoadResult loadData(@NotNull H playerData, boolean force) {
        return loadFromObject(playerData, getUserFile(playerData).getContent(), true);
    }

    @NotNull
    protected abstract DataLoadResult loadFromObject(@NotNull H playerData, @NotNull JsonObject json, boolean isSaved);

    @Override
    public void confirmReception(@NotNull H playerData) {
        // Nothing
    }

    private JsonFile getUserFile(H playerData) {
        return new JsonFile(owning, "userdata", playerData.getEffectiveId().toString());
    }

    @Override
    public List<UUID> retrieveAllPlayerIds() {
        var files = FileUtils.getFile(owning, "userdata").listFiles();
        if (files == null) return Collections.emptyList();

        // Operations on arrays to improve performance
        var collected = new UUID[files.length];
        for (var i = 0; i < files.length; i++)
            collected[i] = UUID.fromString(files[i].getName().split("\\.", 2)[0]);
        return List.of(collected);
    }
}
