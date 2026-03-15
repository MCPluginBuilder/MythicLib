package io.lumine.mythic.lib.comp.interaction.relation;

import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.util.Triplet;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InteractionRules {

    /**
     * Map coding:
     * - First boolean: true if PvP is on, false otherwise.
     * - Second boolean: true if interaction type is offensive, false otherwise.
     * - Third: player relationship.
     */
    private final Map<Triplet<Boolean, Boolean, Relationship>, Boolean> rules = new HashMap<>();

    public final boolean supportSkillsOnMobs;

    public InteractionRules(@Nullable ConfigurationSection config) {
        if (config == null) {
            supportSkillsOnMobs = true;
            return;
        }

        for (Relationship rel : Relationship.values()) {
            rules.put(Triplet.of(true, true, rel), config.getBoolean("pvp_on.offense." + rel.name().toLowerCase(), true));
            rules.put(Triplet.of(true, false, rel), config.getBoolean("pvp_on.support." + rel.name().toLowerCase(), true));
            rules.put(Triplet.of(false, true, rel), config.getBoolean("pvp_off.offense." + rel.name().toLowerCase(), true));
            rules.put(Triplet.of(false, false, rel), config.getBoolean("pvp_off.support." + rel.name().toLowerCase(), true));
        }

        // Hard coded
        for (Relationship rel : Relationship.values())
            if (rel != Relationship.SELF)
                rules.put(Triplet.of(false, true, rel), false);
        rules.put(Triplet.of(true, true, Relationship.PARTY_OTHER), true);
        rules.put(Triplet.of(true, true, Relationship.GUILD_ENEMY), true);

        // Other options
        supportSkillsOnMobs = config.getBoolean("support_skills_on_mobs");
    }

    /**
     * @param pvpEnabled   If PvP is enabled at a specific location
     * @param interaction  Type of player interaction
     * @param relationship Relationship between the players
     * @return If this specific interaction is enabled
     */
    public boolean isEnabled(boolean pvpEnabled, @NotNull InteractionType interaction, @NotNull Relationship relationship) {
        final @Nullable Boolean setting = rules.get(Triplet.of(pvpEnabled, interaction.isOffense(), relationship));
        return setting == null || setting;
    }
}
