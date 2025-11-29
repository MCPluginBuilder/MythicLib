package io.lumine.mythic.lib.damage.mitigation;

import io.lumine.mythic.lib.api.event.DamageMitigationEvent;
import io.lumine.mythic.lib.api.event.mitigation.PlayerBlockEvent;
import io.lumine.mythic.lib.api.event.mitigation.PlayerDodgeEvent;
import io.lumine.mythic.lib.api.event.mitigation.PlayerParryEvent;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import org.bukkit.event.entity.EntityDamageEvent;

@BackwardsCompatibility(version = "1.7.1-SNAPSHOT")
public enum LegacyMitigationType {
    BLOCK, PARRY, DODGE;

    public DamageMitigationEvent generateLegacyEvent(MMOPlayerData playerData, EntityDamageEvent bukkitEvent, MitigationType type) {
        switch (this) {
            case BLOCK:
                return new PlayerBlockEvent(playerData, bukkitEvent, type);
            case PARRY:
                return new PlayerParryEvent(playerData, bukkitEvent, type);
            case DODGE:
                return new PlayerDodgeEvent(playerData, bukkitEvent, type);
            default:
                throw new IllegalArgumentException("Unknown mitigation type: " + this);
        }
    }
}
