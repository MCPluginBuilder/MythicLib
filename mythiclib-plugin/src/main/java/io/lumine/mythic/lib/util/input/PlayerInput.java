package io.lumine.mythic.lib.util.input;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.Tasks;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public abstract class PlayerInput implements Listener {
    protected final Player player;
    protected final Plugin plugin;

    protected final Runnable onCancel;
    protected final Predicate<String> onInput;

    protected boolean open = false;

    protected PlayerInput(Plugin plugin, Player player, Runnable onCancel, Predicate<String> onInput) {
        this.plugin = plugin;
        this.player = player;
        this.onCancel = onCancel;
        this.onInput = onInput;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Processes the player input, closes the edition process if needed and
     * opens the previously opened GUI if needed. This method is protected
     * because it should only be run by edition process classes.
     * For security this method should be called on the main server thread.
     *
     * @param input Player input
     */
    protected void handlePlayerInput(@NotNull String input) {
        Tasks.runSync(this.plugin, () -> {

            // Simply cancel
            if (input.equalsIgnoreCase("cancel")) {
                close();
                if (this.onCancel != null) this.onCancel.run();
            }

            // If return true, cancel.
            else if (this.onInput.test(input)) {
                this.close();
            }
        });
    }

    protected abstract void onClosed();

    protected abstract void onInit();

    public void init() {
        Validate.isTrue(!open, "Player input closed");
        open = true;

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        onInit();
    }

    public void close() {
        Validate.isTrue(open, "Player input already closed");
        open = false;

        HandlerList.unregisterAll(this);
        onClosed();

        if (this.onCancel != null) onCancel.run();
    }

    @NotNull
    public static PlayerInput open(@NotNull Player player, @NotNull Predicate<String> onInput) {
        return open(Type.CHAT, player, onInput);
    }

    @NotNull
    public static PlayerInput open(@NotNull Type type, @NotNull Player player, @NotNull Predicate<String> onInput) {
        return open(type, player, UtilityMethods.emptyRunnable(), onInput);
    }

    @NotNull
    public static PlayerInput open(@NotNull Type type, @NotNull Player player, @NotNull Runnable onCancel, @NotNull Predicate<String> onInput) {
        return open(MythicLib.plugin, type, player, onCancel, onInput);
    }

    @NotNull
    public static PlayerInput open(@NotNull Plugin plugin, @NotNull Type type, @NotNull Player player, @NotNull Runnable onCancel, @NotNull Predicate<String> onInput) {
        switch (type) {
            case CHAT: {
                var input = new ChatPlayerInput(plugin, player, onCancel, onInput);
                input.init();
                return input;
            }
            case ANVIL: {
                var input = new AnvilPlayerInput(plugin, player, onCancel, onInput);
                input.init();
                return input;
            }
            default:
                throw new IllegalArgumentException("Unsupported player input type");
        }
    }

    public static enum Type {
        CHAT,
        ANVIL
    }
}
