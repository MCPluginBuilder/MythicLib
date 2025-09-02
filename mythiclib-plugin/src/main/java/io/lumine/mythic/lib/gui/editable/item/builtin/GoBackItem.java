package io.lumine.mythic.lib.gui.editable.item.builtin;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.placeholder.EmptyPlaceholders;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class GoBackItem<T extends GeneratedInventory> extends PhysicalItem<T> {
    private final Sound clickSound;

    public GoBackItem(ConfigurationSection config) {
        super(config);

        clickSound = config.contains("click_sound") ? Sounds.fromName(UtilityMethods.enumName(config.getString("click_sound"))) : null;
    }

    @Override
    public @NotNull Placeholders getPlaceholders(T inv, int n) {
        return new EmptyPlaceholders();
    }

    @Override
    public void onClick(@NotNull T inv, @NotNull InventoryClickEvent event) {
        if (clickSound != null) inv.getPlayer().playSound(inv.getPlayer().getLocation(), clickSound, 1, 1);
        inv.getNavigator().unblockClosing(); // Safeguard
        inv.getNavigator().popOpen();
    }
}
