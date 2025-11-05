package io.lumine.mythic.lib.player.cooldown;

public enum CooldownType implements CooldownObject{

    // Damage mitigation
    DODGE,
    PARRY,
    BLOCK,

    // Critical strikes
    WEAPON_CRIT,
    SKILL_CRIT;

    @Override
    public String getCooldownPath() {
        return name();
    }
}
