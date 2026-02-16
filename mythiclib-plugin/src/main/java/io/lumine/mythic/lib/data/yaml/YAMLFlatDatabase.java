package io.lumine.mythic.lib.data.yaml;

import io.lumine.mythic.lib.data.Database;
import io.lumine.mythic.lib.data.OfflineDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.queue.DataLoadResult;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.profile.SessionUpdateReason;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.config.YamlFile;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class YAMLFlatDatabase<H extends SynchronizedDataHolder, O extends OfflineDataHolder> implements Database<H, O> {
    private final MMOPlugin owning;

    public YAMLFlatDatabase(MMOPlugin owning) {
        this.owning = Objects.requireNonNull(owning, "Plugin cannot be null");
    }

    @Override
    public @NotNull MMOPlugin getPlugin() {
        return owning;
    }

    @Override
    public void saveData(@NotNull H playerData, @NotNull SessionUpdateReason reason) {
        final var ymlFile = new YamlFile(owning, "userdata", playerData.getEffectiveId().toString(), false);
        saveInSection(playerData, ymlFile.getContent());
        ymlFile.save();
    }

    public abstract void saveInSection(@NotNull H playerData, @NotNull ConfigurationSection config);

    @NotNull
    @Override
    public DataLoadResult loadData(@NotNull H playerData, boolean force) {
        final var ymlFile = new YamlFile(owning, "userdata", playerData.getEffectiveId().toString());
        return loadFromSection(playerData, ymlFile.getContent(), true);
    }

    @Override
    public void confirmReception(@NotNull H playerData) {
        // Nothing
    }

    @NotNull
    protected abstract DataLoadResult loadFromSection(@NotNull H playerData, @NotNull ConfigurationSection config, boolean isSaved);

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
