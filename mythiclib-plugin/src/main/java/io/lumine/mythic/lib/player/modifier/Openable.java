package io.lumine.mythic.lib.player.modifier;

import io.lumine.mythic.lib.api.player.MMOPlayerData;

/**
 * Interface used for player modifiers. A closable
 * player modifier is a modifier that requires some
 * action to be run whenever it is registered
 * in the player's modifier map.
 * <p>
 * For instance, abilities running on a timer need
 * to initialize and start a bukkit task.
 *
 * @author jules
 * @deprecated Not used anymore
 */
@Deprecated
public interface Openable {

    @Deprecated
    void open(MMOPlayerData player);
}
