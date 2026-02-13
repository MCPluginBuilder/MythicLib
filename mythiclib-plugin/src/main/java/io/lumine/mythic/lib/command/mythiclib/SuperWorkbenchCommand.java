package io.lumine.mythic.lib.command.mythiclib;

import io.lumine.mythic.lib.api.crafting.recipes.vmp.SuperWorkbenchMapping;
import io.lumine.mythic.lib.api.util.ui.FFPMythicLib;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeRoot;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SuperWorkbenchCommand extends CommandTreeRoot {
    public SuperWorkbenchCommand(ConfigurationSection config) {
        super(config);
    }

    @Override
    public @NotNull CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {

        // All right, lets identify our args
        Player target = null;
        boolean failure = false;
        FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMythicLib.get());
        ffp.activatePrefix(true, "Super Workbench");

        // No args specified, well oops
        if (args.length == 0) {

            // Command Sender applicable?
            if (sender instanceof Player) {

                // That shall be the target
                target = (Player) sender;

                // Failure
            } else {

                // RIP
                failure = true;

                // Add fail message
                ffp.log(FriendlyFeedbackCategory.ERROR, "$fYou must specify a player when calling from the console. $bBy the way, you can download the free texture pack assets to make the glass borders look smooth at https://sites.google.com/view/gootilities/core-plugin-goop/containers/container-templates/edge-formations?authuser=0");
            }

            // Find that player
        } else {

            // Identify name
            String name = args[0];

            // Can you get a UUID from it?
            UUID possibleUUID = SilentNumbers.UUIDParse(name);
            if (possibleUUID != null) {
                target = Bukkit.getPlayer(possibleUUID);
            }

            // Still null?
            if (target == null) {

                // First attempt as get player exact o/
                target = Bukkit.getPlayerExact(name);

                // Still null?
                if (target == null) {

                    // RIP
                    failure = true;

                    // Add fail message
                    ffp.log(FriendlyFeedbackCategory.ERROR, "Player $i{0}$b not found.", name);
                }
            }
        }

        // All right, ready?
        if (!failure) {

            // Open that station to them
            Inventory swb = SuperWorkbenchMapping.getSuperWorkbench(target);

            // Open it
            target.openInventory(swb);

            return CommandResult.SUCCESS;

            // Log errors
        } else {

            // Log messages
            if (sender instanceof Player) {
                ffp.sendAllTo((Player) sender);
            } else {
                ffp.sendAllTo(sender);
            }
        }

        // :gruno:
        return CommandResult.FAILURE;
    }
}
