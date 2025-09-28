package io.lumine.mythic.lib.profile;

/**
 * Different states of a player session.
 */
public enum PlayerSessionState {

    INIT,

    /**
     * Player just logged in. If a profile plugin is installed,
     * the MMO plugins will NOT load data yet so player session
     * will stay at this state.
     * <p>
     * Possible transitions:
     * - {@link #DEAD} if the player logs off before the player
     * session as been marked {@link #OPEN}.
     * - {@link #OPEN} if all MMO plugins successfully loaded their
     * data. For MythicLib the player has officially "started playing".
     */
    OPENING,

    /**
     * Player logged in and, if a profile plugin is installed, has
     * chosen a profile and started playing.
     * <p>
     * Possible transitions:
     * - {@link #CLOSING} when the player logs off or switches profile.
     */
    OPEN,

    /**
     * If the same player logs in again, they need to wait for
     * the previous session to terminate.
     * <p>
     * Possible transitions:
     * - Player session transitions to
     */
    CLOSING,

    /**
     * All MMO plugins have successfully written back their
     * data to the database and the session is fully invalidated.
     * <p>
     * If the same player logs back, they can re-open a new session
     * at state {@link #OPENING}.
     * <p>
     * Possible transitions: None (final state).
     */
    DEAD
}
