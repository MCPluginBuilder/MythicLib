package io.lumine.mythic.lib.version;

import io.lumine.mythic.lib.UtilityMethods;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum VMaterial {
    BLAST_FURNACE("BLAST_FURNACE", "FURNACE"),
    CAMPFIRE("CAMPFIRE", "FURNACE"),
    SMOKER("SMOKER", "FURNACE"),
    SMITHING_TABLE("SMITHING_TABLE", "FURNACE"),
    GRASS_BLOCK("GRASS_BLOCK", "GRASS"),
    SPYGLASS("SPYGLASS", "GLASS_BOTTLE"),

    ;

    private final Material wrapped;

    VMaterial(String... candidates) {
        wrapped = UtilityMethods.resolveField(Material::valueOf, candidates);
    }

    @NotNull
    public Material get() {
        return wrapped;
    }

    @NotNull
    public ItemStack toItem() {
        return new ItemStack(wrapped);
    }
}
