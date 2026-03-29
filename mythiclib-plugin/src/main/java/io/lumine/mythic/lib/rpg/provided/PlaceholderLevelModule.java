package io.lumine.mythic.lib.rpg.provided;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.jetbrains.annotations.NotNull;

public class PlaceholderLevelModule implements LevelModule {
    private final String placeholderFormula;

    public PlaceholderLevelModule(String placeholderFormula) {
        this.placeholderFormula = placeholderFormula;
    }

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        var raw = MythicLib.plugin.getPlaceholderParser().parse(player.getPlayer(), placeholderFormula);
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return 1;
        }
    }
}
