package io.lumine.mythic.lib.command.builtin;

import io.lumine.mythic.lib.command.CommandTreeExplorer;
import io.lumine.mythic.lib.command.CommandTreeNode;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.data.*;
import io.lumine.mythic.lib.util.Lazy;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.io.SafeBukkitObjectOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 1.20.4 changed the way NBT tags are stored inside item NBTs. Spigot
 * does not provide backwards compatibility for external NBT paths like
 * "MMOITEMS_xxxx" or literally any other plugin. This creates a major issue
 * when upgrading from 1.20.4 to 1.20.5+ as all NBT-related item data is lost,
 * including MMOItems item data.
 */
@BackwardsCompatibility(version = "1.20.4")
public class ConvertItemNBTCommandNode<H extends SynchronizedDataHolder, O extends OfflineDataHolder> extends CommandTreeNode {

    @Nullable
    private final String permission;
    private final Supplier<String> permissionMessage;

    private final SynchronizedDataManager<H, O> dataManager;

    private final Argument<String> argEnable;

    public ConvertItemNBTCommandNode(CommandTreeNode parent,
                                     @NotNull SynchronizedDataManager<H, O> dataManager,
                                     @Nullable String permission,
                                     @Nullable Supplier<String> permissionMessage) {
        super(parent, "convert-item-nbts");

        this.permission = permission;
        this.permissionMessage = permissionMessage;
        this.dataManager = dataManager;

        this.argEnable = addArgument(Argument.choices("enable", "to", "from"));
    }

    @NotNull
    @Override
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        if (permission != null && !sender.hasPermission(permission)) return explorer.fail(permissionMessage.get());

        final var rawEnableArg = explorer.parse(argEnable);
        final var toNbtApi = rawEnableArg.equalsIgnoreCase("to");
        final String fromString = toNbtApi ? "Bukkit" : "NBTAPI", toString = toNbtApi ? "NBTAPI" : "Bukkit";

        if (toNbtApi && Bukkit.getPluginManager().getPlugin("NBTAPI") == null) {
            return explorer.fail("You are asking to convert item data to NBTAPI however the plugin is not installed. Please install NBTAPI and try again.");
        }

        explorer.verbose("Converting data to NBTAPI...");

        // Export data from/to the same data source (ingenious!!)
        Lazy<Database<H, O>> dataHandlerLazy = Lazy.of(dataManager.getDatabase());
        try {
            SafeBukkitObjectOutputStream.USE_NBT_API = toNbtApi;
            explorer.verbose("Using NBTAPI: " + toNbtApi);
            DataExport<H, O> work = new DataExport<>(dataManager, sender);
            work.setCallback(() -> {
                SafeBukkitObjectOutputStream.USE_NBT_API = false;
                explorer.verbose(String.format("Successfully converted item data from %s to %s. Please now restart your server.", fromString, toString));
            });

            boolean startResult = work.start(dataHandlerLazy, dataHandlerLazy);

            if (startResult) {
                explorer.verbose(String.format("Item data conversion from %s to %s started, please wait... ", fromString, toString));
                return CommandResult.SUCCESS;
            }

            return CommandResult.FAILURE;

        } catch (Exception throwable) {
            explorer.verbose("An error occurred: " + throwable.getMessage());
            explorer.verbose("Please check console for more information.");
            throwable.printStackTrace();
            return CommandResult.FAILURE;
        }
    }
}
