package io.lumine.mythic.lib.version.impl;

import io.lumine.mythic.lib.version.VInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class ModernInventoryViewImpl implements VInventoryView {
    public final InventoryView view;

    public ModernInventoryViewImpl(InventoryView view) {
        this.view = view;
    }

    @Override
    public String getTitle() {
        return view.getTitle();
    }

    @Override
    public InventoryType getType() {
        return view.getType();
    }

    @Override
    public Inventory getTopInventory() {
        return view.getTopInventory();
    }


    @Override
    public Inventory getBottomInventory() {
        return view.getBottomInventory();
    }

    @Override
    public void setCursor(ItemStack actualCursor) {
        view.setCursor(actualCursor);
    }

    @Override
    public HumanEntity getPlayer() {
        return view.getPlayer();
    }

    @Override
    public void close() {
        view.close();
    }
}
