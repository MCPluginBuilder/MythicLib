package io.lumine.mythic.lib.profile.handler;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.module.MMOPlugin;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoProfileHandler implements ProfileHandler {
    private final List<NamespacedKey> modules;

    public NoProfileHandler() {
        modules = MythicLib.plugin.getMMOPlugins().stream().map(MMOPlugin::getNamespacedKey).collect(Collectors.toList());
    }

    @Override
    public List<NamespacedKey> collectModules() {
        return new ArrayList<>(this.modules);
    }
}
