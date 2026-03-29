package io.lumine.mythic.lib.rpg.compat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.LevelModule;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.players.PlayerManager;

public class McRPG implements LevelModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        try {
            return PlayerManager.getPlayer(player.getUniqueId()).getPowerLevel();
        } catch (Exception exception) {
            return 0;
        }
    }
}