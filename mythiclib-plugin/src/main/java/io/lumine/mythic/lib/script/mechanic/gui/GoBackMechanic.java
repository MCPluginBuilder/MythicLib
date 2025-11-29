package io.lumine.mythic.lib.script.mechanic.gui;

import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.version.VersionUtils;
import org.jetbrains.annotations.NotNull;

@MechanicMetadata
public class GoBackMechanic extends Mechanic {
    public GoBackMechanic() {
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        final var player = meta.getCaster().getPlayer();
        var topInventoryHolder = VersionUtils.getOpen(player).getTopInventory().getHolder();
        if (topInventoryHolder instanceof GeneratedInventory)
            ((GeneratedInventory) topInventoryHolder).getNavigator().popOpen();
    }
}

