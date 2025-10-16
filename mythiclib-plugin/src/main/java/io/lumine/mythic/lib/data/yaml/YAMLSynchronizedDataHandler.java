package io.lumine.mythic.lib.data.yaml;

import io.lumine.mythic.lib.data.*;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.config.YamlFile;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class YAMLSynchronizedDataHandler<H extends SynchronizedDataHolder, O extends OfflineDataHolder> implements SynchronizedDataHandler<H, O> {
    private final MMOPlugin owning;

    public YAMLSynchronizedDataHandler(MMOPlugin owning) {
        this.owning = Objects.requireNonNull(owning, "Plugin cannot be null");
    }

    @Override
    public void saveData(@NotNull H playerData, @NotNull SaveReason reason) {
        // TODO YML object is loaded in memory, useless
        final YamlFile configFile = getUserFile(playerData);
        saveInSection(playerData, configFile.getContent());
        configFile.save();
    }

    public abstract void saveInSection(@NotNull H playerData, @NotNull ConfigurationSection config);

    @NotNull
    @Override
    public DataLoadResult loadData(@NotNull H playerData, boolean force) {
        return loadFromSection(playerData, getUserFile(playerData).getContent(), true);
    }

    @Override
    public void confirmReception(@NotNull H playerData) {
        // Nothing
    }

    @NotNull
    protected abstract DataLoadResult loadFromSection(@NotNull H playerData, @NotNull ConfigurationSection config, boolean isSaved);

    @NotNull
    private YamlFile getUserFile(@NotNull H playerData) {
        return new YamlFile(owning, "userdata", playerData.getEffectiveId().toString());
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
