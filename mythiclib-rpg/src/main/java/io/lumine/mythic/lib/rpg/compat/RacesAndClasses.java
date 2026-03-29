package io.lumine.mythic.lib.rpg.compat;

import de.tobiyas.racesandclasses.playermanagement.player.RaCPlayerManager;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.ClassModule;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.jetbrains.annotations.NotNull;

public class RacesAndClasses implements LevelModule, ClassModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        var other = RaCPlayerManager.get().getPlayer(player.getUniqueId());
        return other.getCurrentLevel();
    }

    @Override
    public @NotNull String getClass(@NotNull MMOPlayerData player) {
        var other = RaCPlayerManager.get().getPlayer(player.getUniqueId());
        return other.getclass().getDisplayName();
    }
}
