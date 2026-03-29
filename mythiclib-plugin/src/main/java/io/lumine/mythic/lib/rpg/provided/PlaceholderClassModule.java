package io.lumine.mythic.lib.rpg.provided;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.ClassModule;
import org.jetbrains.annotations.NotNull;

public class PlaceholderClassModule implements ClassModule {
    private final String placeholderFormula;

    public PlaceholderClassModule(String placeholderFormula) {
        this.placeholderFormula = placeholderFormula;
    }

    @Override
    public @NotNull String getClass(@NotNull MMOPlayerData player) {
        return MythicLib.plugin.getPlaceholderParser().parse(player.getPlayer(), placeholderFormula);
    }
}
