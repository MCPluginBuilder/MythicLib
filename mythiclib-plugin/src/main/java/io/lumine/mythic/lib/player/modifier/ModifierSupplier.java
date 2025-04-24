package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ModifierSupplier {

    @NotNull
    public EquipmentSlot getEquipmentSlot();

    @NotNull
    public ModifierSource getModifierSource();

    @NotNull
    public Collection<PlayerModifier> getModifierCache();
}
