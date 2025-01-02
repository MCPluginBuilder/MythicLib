package io.lumine.mythic.lib.util.loadingorder;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyNode {
    public final String name;
    public final int id;
    public final Map<Integer, DependencyNode> children = new HashMap<>();

    public Plugin plugin;

    public DependencyNode(int id, Plugin plugin) {
        this.id = id;
        name = plugin.getName();
        this.plugin = plugin;
    }

    public void loadAdjacency(DependencyCycleCheck checker) {
        Validate.notNull(plugin, "Already loaded");

        // Deps
        final List<String> deps = new ArrayList<>(plugin.getDescription().getDepend());
        deps.addAll(plugin.getDescription().getSoftDepend());
        for (String name : deps) {
            final DependencyNode found = checker.registry.get(name);
            if (found != null) children.put(found.id, found);
        }

        // Loadbefores
        for (String name : plugin.getDescription().getLoadBefore()) {
            final DependencyNode found = checker.registry.get(name);
            if (found != null && !found.children.containsKey(id)) found.children.put(id, this);
        }

        plugin = null;
    }

    @Override
    public String toString() {
        return name;
    }
}
