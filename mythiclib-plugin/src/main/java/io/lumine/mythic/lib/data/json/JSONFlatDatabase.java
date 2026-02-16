package io.lumine.mythic.lib.data.json;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.data.Database;
import io.lumine.mythic.lib.data.OfflineDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.queue.DataLoadResult;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.Jsonable;
import io.lumine.mythic.lib.util.config.JsonFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class JSONFlatDatabase<H extends SynchronizedDataHolder & Jsonable, O extends OfflineDataHolder> implements Database<H, O> {
    private final MMOPlugin owning;

    public JSONFlatDatabase(MMOPlugin owning) {
        this.owning = Objects.requireNonNull(owning, "Plugin cannot be null");
    }

    @Override
    public @NotNull MMOPlugin getPlugin() {
        return owning;
    }

    @Override
    public void saveData(@NotNull H playerData, @NotNull SessionUpdateReason reason) {
        final var jsonFile = new JsonFile(owning, "userdata", playerData.getEffectiveId().toString(), false);
        jsonFile.setContent(playerData.toJson());
        jsonFile.save();
    }

    @NotNull
    @Override
    public DataLoadResult loadData(@NotNull H playerData, boolean force) {
        final var jsonFile = new JsonFile(owning, "userdata", playerData.getEffectiveId().toString());
        return loadFromObject(playerData, jsonFile.getContent(), true);
    }

    @NotNull
    protected abstract DataLoadResult loadFromObject(@NotNull H playerData, @NotNull JsonObject json, boolean isSaved);

    @Override
    public void confirmReception(@NotNull H playerData) {
        // Nothing
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
