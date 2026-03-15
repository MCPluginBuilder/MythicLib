package io.lumine.mythic.lib.comp.interaction.relation;

import io.lumine.mythic.lib.comp.interaction.InteractionType;
import org.jetbrains.annotations.NotNull;

public class EmptyPvPInteractionRules extends InteractionRules {
    public EmptyPvPInteractionRules() {
        super(null);
    }

    /**
     * @param pvpEnabled   If PvP is enabled at a specific location
     * @param interaction  Type of player interaction
     * @param relationship Relationship between the players
     * @return If this specific interaction is enabled
     */
    @Override
    public boolean isEnabled(boolean pvpEnabled, @NotNull InteractionType interaction, @NotNull Relationship relationship) {
        return true;
    }
}
