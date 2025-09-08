package io.lumine.mythic.lib.data.yaml;

import io.lumine.mythic.lib.data.OfflineDataHolder;
import io.lumine.mythic.lib.data.SaveReason;
import io.lumine.mythic.lib.data.SynchronizedDataHandler;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.util.ConfigFile;
import io.lumine.mythic.lib.util.FileUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class YAMLSynchronizedDataHandler<H extends SynchronizedDataHolder, O extends OfflineDataHolder> implements SynchronizedDataHandler<H, O> {
    private final Plugin owning;

    public YAMLSynchronizedDataHandler(Plugin owning) {
        this.owning = Objects.requireNonNull(owning, "Plugin cannot be null");
    }

    @Deprecated
    public YAMLSynchronizedDataHandler(Plugin owning, boolean profilePlugin) {
        this(owning);
    }

    @Override
    public void saveData(@NotNull H playerData, @NotNull SaveReason reason) {
        // TODO YML object is loaded in memory, useless
        final ConfigFile configFile = getUserFile(playerData);
        saveInSection(playerData, configFile.getConfig());
        configFile.save();
    }

    public abstract void saveInSection(@NotNull H playerData, @NotNull ConfigurationSection config);

    @Override
    public boolean loadData(@NotNull H playerData) {
        loadFromSection(playerData, getUserFile(playerData).getConfig());
        return true;
    }

    public abstract void loadFromSection(@NotNull H playerData, @NotNull ConfigurationSection config);

    private ConfigFile getUserFile(@NotNull H playerData) {
        return new ConfigFile(owning, "/userdata", playerData.getEffectiveId().toString());
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
