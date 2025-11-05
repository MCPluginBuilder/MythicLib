package io.lumine.mythic.lib.gui.editable.item.builtin;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.ItemOptions;
import io.lumine.mythic.lib.gui.editable.item.PhysicalItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.placeholder.EmptyPlaceholders;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Sounds;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NextPageItem<T extends GeneratedInventory> extends PhysicalItem<T> {
    private final Sound clickSound;
    private final boolean hideIfNoPage;
    private final PhysicalItem<T> noPage;

    public NextPageItem(ConfigurationSection config) {
        super(config);

        clickSound = config.contains("click_sound") ? Sounds.fromName(UtilityMethods.enumName(config.getString("click_sound"))) : null;
        hideIfNoPage = config.getBoolean("hide_if_no_page", true);
        noPage = config.contains("no_page") ? new SimpleItem<>(this, config.getConfigurationSection("no_page")) : null;
    }

    @Override
    public @NotNull Placeholders getPlaceholders(T inv, int n) {
        return new EmptyPlaceholders();
    }

    @Override
    public boolean isDisplayed(@NotNull T inv) {
        Validate.isTrue(inv.hasPagination(), "Pagination disabled");
        return !hideIfNoPage || isWithinBounds(inv);
    }

    private boolean isWithinBounds(@NotNull T inv) {
        return inv.page < inv.getMaxPage();
    }

    @Override
    public @Nullable ItemStack getDisplayedItem(T inv, ItemOptions options) {

        // No page display
        if (noPage != null && !isWithinBounds(inv)) return noPage.getDisplayedItem(inv, options);

        return super.getDisplayedItem(inv, options);
    }

    @Override
    public void onClick(@NotNull T inv, @NotNull InventoryClickEvent event) {

        // Check bounds first
        if (inv.page >= inv.getMaxPage()) return;

        inv.page++;
        if (clickSound != null) inv.getPlayer().playSound(inv.getPlayer().getLocation(), clickSound, 1, 1);
        inv.open();
    }
}
