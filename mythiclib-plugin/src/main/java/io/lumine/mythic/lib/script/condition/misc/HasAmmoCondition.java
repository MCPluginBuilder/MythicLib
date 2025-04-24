package io.lumine.mythic.lib.script.condition.misc;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Checks if the current world time is DAY/NIGHT/DUSK..
 */
public class HasAmmoCondition extends Condition {
    private final Material item;
    private final boolean creativeInfinite, consumeIfMet;
    private final String itemIgnoreTag;

    public HasAmmoCondition(ConfigObject config) {
        super(config);

        config.validateKeys("item");

        item = Material.valueOf(config.getString("item"));
        creativeInfinite = config.getBoolean("creative_infinite", false);
        itemIgnoreTag = config.contains("item_ignore_tag") ? config.getString("item_ignore_tag") : null;
        consumeIfMet = config.getBoolean("consume_if_met", false);
    }

    @Override
    public boolean isMet(SkillMetadata meta) {

        // If creative, return true without consuming
        if (creativeInfinite && meta.getCaster().getPlayer().getGameMode() == GameMode.CREATIVE) return true;

        // Check for item consumption ignore tag
        if (itemIgnoreTag != null && !itemIgnoreTag.isEmpty() && NBTItem.get(meta.getCaster().getPlayer().getInventory().getItem(meta.getCaster().getActionHand().toBukkit())).getBoolean(itemIgnoreTag))
            return true;

        // Check if player has ammo
        if (!meta.getCaster().getPlayer().getInventory().containsAtLeast(new ItemStack(item), 1)) return false;

        // Consume ammo if needed
        if (consumeIfMet) meta.getCaster().getPlayer().getInventory().removeItem(new ItemStack(item));
        return true;
    }
}