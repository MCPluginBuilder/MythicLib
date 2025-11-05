package io.lumine.mythic.lib.script.variable;

/**
 * In which environment the skill variable is being saved.
 */
public enum VariableScope {

    /**
     * Server-level variables are shared by all players,
     * skills or profiles.
     */
    SERVER,

    /**
     * Player-level variables are shared by all skills and profiles.
     * They are not lost if the player switches profiles, but disappear
     * if the player logs out for too long.
     */
    PLAYER,

    /**
     * Profile-level variables are shared by all skills. They are lost
     * when the player switches profiles, and reappear if the player
     * enters the same profile again. They are lost if the player
     * logs out of the profile for too long.
     */
    PROFILE,

    /**
     * Skill-level variables are only stored when the skill is being
     * cast. They are lost when the skill executes its last mechanic.
     */
    SKILL
}
