package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.skill.SimpleSkill;
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

        script = config.contains("on-click") ? MythicLib.plugin.getSkills().loadScript(config.get("on-click")) : null;
    }

    @Override
    public @NotNull Placeholders getPlaceholders(T inv, int n) {
        return new Placeholders();
    }

    @Override
    public void onClick(@NotNull T inv, @NotNull InventoryClickEvent event) {
        if (script != null) {
            SimpleSkill skill = new SimpleSkill(script);
            skill.cast(inv.getMMOPlayerData());
        }
    }
}
