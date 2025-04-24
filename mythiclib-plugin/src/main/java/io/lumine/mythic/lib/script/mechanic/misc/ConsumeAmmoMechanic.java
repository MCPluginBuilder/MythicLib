package io.lumine.mythic.lib.script.mechanic.misc;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Checks if the current world time is DAY/NIGHT/DUSK..
 */
public class ConsumeAmmoMechanic extends Mechanic {
    private final Material item;
    private final boolean creativeInfinite;
    private final String itemIgnoreTag;

    public ConsumeAmmoMechanic(ConfigObject config) {
        config.validateKeys("item");

        item = Material.valueOf(config.getString("item"));
        creativeInfinite = config.getBoolean("creative_infinite", false);
        itemIgnoreTag = config.contains("item_ignore_tag") ? config.getString("item_ignore_tag") : null;
    }

    @Override
    public void cast(SkillMetadata meta) {

        // If creative, no consume
        if (creativeInfinite && meta.getCaster().getPlayer().getGameMode() == GameMode.CREATIVE) return;

        // Check for item consumption ignore tag
        if (itemIgnoreTag != null && !itemIgnoreTag.isEmpty() && NBTItem.get(meta.getCaster().getPlayer().getInventory().getItem(meta.getCaster().getActionHand().toBukkit())).getBoolean(itemIgnoreTag))
            return;

        // Consume ammo
        meta.getCaster().getPlayer().getInventory().removeItem(new ItemStack(item));
    }
}