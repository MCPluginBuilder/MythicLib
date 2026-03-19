package io.lumine.mythic.lib.util.input;

import io.lumine.mythic.lib.util.lang3.Validate;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class AnvilPlayerInput extends PlayerInput implements Listener {

    @Nullable
    private AnvilGUI handle;

    public AnvilPlayerInput(Plugin plugin, Player player, Runnable onCancel, Predicate<String> onInput) {
        super(plugin, player, onCancel, onInput);
    }

    @Override
    protected void onInit() {
        Validate.isTrue(handle == null, "Already initialized");

        var inputHandler = this;

        handle = new AnvilGUI.Builder()
                .onClose(stateSnapshot -> inputHandler.close())
                .onClick((slot, stateSnapshot) -> {
                    if (slot == AnvilGUI.Slot.OUTPUT) inputHandler.handlePlayerInput(stateSnapshot.getText());
                    return List.of();
                })
                //.preventClose()
                .text("Write here...")
                .title("Player Input")
                .plugin(plugin)
                .open(player);
    }

    @Override
    protected void onClosed() {
        Validate.notNull(handle, "Handle not found");

        handle.closeInventory();
        handle = null;
    }
}