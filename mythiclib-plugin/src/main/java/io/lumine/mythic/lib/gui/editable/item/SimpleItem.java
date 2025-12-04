package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.placeholder.EmptyPlaceholders;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.config.YamlUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An inventory item that has no particular placeholder
 * yet it DOES support PAPI placeholders.
 */
public class SimpleItem<T extends GeneratedInventory> extends PhysicalItem<T> {
    private final Script script;

    public SimpleItem(@NotNull ConfigurationSection config) {
        this(null, config);
    }

    public SimpleItem(@Nullable InventoryItem<T> parent, @NotNull ConfigurationSection config) {
        super(parent, config);

        var scriptRaw = YamlUtils.get(config, "on-click", "on_click");
        script = scriptRaw != null ? MythicLib.plugin.getSkills().loadScript(scriptRaw) : null;
    }

    @Override
    public @NotNull Placeholders getPlaceholders(T inv, int n) {
        return new EmptyPlaceholders();
    }

    @Override
    public void onClick(@NotNull T inv, @NotNull InventoryClickEvent event) {
        if (script != null) script.cast(SkillMetadata.of(inv.getMMOPlayerData()));
    }
}
