package io.lumine.mythic.lib.comp.placeholder;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.comp.placeholder.api.PlaceholderEntry;
import io.lumine.mythic.lib.comp.placeholder.api.PluginPlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

public class MythicLibExpansion extends PluginPlaceholderExpansion<MMOPlayerData> {
    public MythicLibExpansion(MythicLib owner) {
        super(owner);
    }

    @Override
    protected @NotNull Iterable<PlaceholderEntry<MMOPlayerData>> getPlaceholderRegistry() {
        return Arrays.asList(PlaceholderEnum.values());
    }

    @Override
    protected @NonNull MMOPlayerData getPlayerData(OfflinePlayer player) {
        return MMOPlayerData.get(player);
    }
}
