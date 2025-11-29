package io.lumine.mythic.lib.script.mechanic.gui;

import io.lumine.mythic.lib.script.mechanic.Mechanic;
import io.lumine.mythic.lib.script.mechanic.MechanicMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import org.jetbrains.annotations.NotNull;

@MechanicMetadata
public class CloseInventoryMechanic extends Mechanic {
    public CloseInventoryMechanic() {
    }

    @Override
    public void cast(@NotNull SkillMetadata meta) {
        meta.getCaster().getPlayer().closeInventory();
    }
}
