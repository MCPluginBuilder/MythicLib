package io.lumine.mythic.lib.rpg.compat;

import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.rpg.LevelModule;
import me.baks.rpl.PlayerList;
import org.jetbrains.annotations.NotNull;

/**
 * This plugin is old AF... still implements player data
 * storage using player names instead of UUIDs
 */
public class RPGPlayerLeveling implements LevelModule {

    @Override
    public int getLevel(@NotNull MMOPlayerData player) {
        var other = PlayerList.getByName(player.getPlayer().getName());
        return other == null ? 0 : other.getPlayerLevel();
    }
}