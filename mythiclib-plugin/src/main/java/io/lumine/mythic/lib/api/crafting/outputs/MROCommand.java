package io.lumine.mythic.lib.api.crafting.outputs;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.crafting.event.MythicCraftItemEvent;
import io.lumine.mythic.lib.api.crafting.ingredients.MythicBlueprintInventory;
import io.lumine.mythic.lib.api.crafting.ingredients.MythicIngredient;
import io.lumine.mythic.lib.api.crafting.ingredients.MythicRecipeInventory;
import io.lumine.mythic.lib.api.crafting.recipes.MythicCachedResult;
import io.lumine.mythic.lib.api.crafting.recipes.vmp.VanillaInventoryMapping;
import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.util.ui.FFPMythicLib;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

/**
 * When the player crafts something, and casts a command
 * rather than getting a new item. Very interesting.
 *
 * @author Gunging
 */
@SuppressWarnings("unused")
public class MROCommand extends MythicRecipeOutput {

    /**
     * The name that will override that of the display item.
     */
    @Nullable final String displayedName;
    /**
     * The name that will override that of the display item.
     */
    @Nullable public String getDisplayedName() { return displayedName; }
    /**
     * The description that will override the lore of the display item
     */
    @Nullable final ArrayList<String> description;
    /**
     * The description that will override the lore of the display item
     */
    @Nullable public ArrayList<String> getDescription() {return description;}
    /**
     * The display item that the player may pick up.
     */
    @NotNull final MythicIngredient display;
    /**
     * The display item that the player may pick up.
     */
    @NotNull public MythicIngredient getDisplay() {return display;}
    /**
     * The commands that will run on craft.
     */
    @NotNull final ArrayList<String> commands;
    /**
     * The commands that will run on craft.
     */
    @NotNull public ArrayList<String> getCommands() {return commands;}
    /**
     * if deterministic, the item to show.
     */
    @Nullable ItemStack displayItem;
    /**
     * if deterministic, the item to show.
     */
    @Nullable public ItemStack getDisplayItem() {return displayItem;}

    /**
     * MROCommand runs a command when the player crafts an item.
     *
     * @param displayedName Name override of the item that will be displayed.
     *
     * @param description Lore override of the item that will be displayed
     *
     * @param display The item that will be displayed
     *
     * @param commands The list of commands to run
     *
     * @throws IllegalArgumentException When the MythicIngredient does not have a single valid determinate substitute.
     *                                  (That it cant generate an item)
     */
    public MROCommand(@Nullable String displayedName, @Nullable String description, @NotNull MythicIngredient display, @NotNull String... commands) throws IllegalArgumentException {
        this.displayedName = displayedName;
        if (description != null) { this.description = SilentNumbers.chop(description, 50, "\u00a7e\u00a7o"); } else { this.description = null; }
        this.display = display;
        this.commands = SilentNumbers.addAll(null, commands);

        // Fail
        if (display.getRandomSubstituteItem(null) == null) { throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMythicLib.get(), "Could not generate $fCommand Recipe Result$b: You must specify a valid UIFilter that generates an Item Stack!")); }

        // Is it deterministic?
        if (display.getSubstitutes().size() == 1) {

            // Attempt to get
            ProvidedUIFilter sub = display.getRandomSubstitute(true);

            // Yes
            if (sub != null) {

                // Generate iem
                ItemStack gen = sub.getDisplayStack(null);

                // Apply changes
                @NotNull ItemMeta gMeta = Objects.requireNonNull(gen.getItemMeta());

                // Edit Stack
                if (displayedName != null) { gMeta.setDisplayName(MythicLib.plugin.parseColors(displayedName)); }
                if (description != null) { gMeta.setLore(this.description); }

                // Put
                gen.setItemMeta(gMeta);

                // Set
                displayItem = gen;
            }
        }
    }

    @NotNull @Override public MythicRecipeInventory applyDisplay(@NotNull MythicBlueprintInventory inventory, @NotNull InventoryClickEvent eventTrigger, @NotNull VanillaInventoryMapping mapping) {
        ItemStack git = getDisplayItem();
        if (git == null) { git = getDisplay().getRandomDisplayItem(null); }
        MythicRecipeInventory edited = inventory.getResultInventory().clone();
        edited.clean();
        edited.setItemAt(0, 0, git);
        return edited; }

    @Override
    public void applyResult(@NotNull MythicRecipeInventory resultInventory, @NotNull MythicBlueprintInventory otherInventories, @NotNull MythicCachedResult cache, @NotNull InventoryClickEvent eventTrigger, @NotNull VanillaInventoryMapping map, int times) {

        /*
         * Listen, we'll take it from here. Cancel the original event.
         *
         * Now run Mythic Crafting version of the event.
         * Did anyone cancel it? Well I guess we'll touch nothing then, but you also cant craft this item >:I
         */
        eventTrigger.setCancelled(true);
        if (times > 1 && !(eventTrigger.getWhoClicked() instanceof Player)) { return; }
        MythicCraftItemEvent preEvent = new MythicCraftItemEvent(eventTrigger, resultInventory, map, otherInventories, cache);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) { return; }

        // Parse command list
        ArrayList<String> parsed = new ArrayList<>();
        for (String str : getCommands()) {
            if (str == null) { continue; }

            /*
             * Parses all Placeholders of the usual form, %<something>%,
             * and todo parses the sender location for MythicLib commands
             */
            parsed.add(MythicLib.plugin.getPlaceholderParser().parse((OfflinePlayer) eventTrigger.getWhoClicked(), str));
        }

        // Run command list for each time
        for (int t = 1; t <= times; t++) {

            /*
             * Run every command. Usually we'd pass in the sender's location
             * as an argument in there, but because we parsed all commands
             * beforehand, we should be doing the location addition as well.
             */
            for (String command : parsed) { SilentNumbers.executeCommand(MythicLib.plugin.getServer().getConsoleSender(), command, null); }
        }

        /*
         * This is the part where we'd check that the commands succeeded, and how many times they did or whatever.
         * However, we don't have successible commands yet so that's not done yet.
         *
         * All right well, consume the ingredients :wazowskibruhmoment:
         */
        consumeIngredients(otherInventories, cache, eventTrigger.getInventory(), map, times);
    }
}
