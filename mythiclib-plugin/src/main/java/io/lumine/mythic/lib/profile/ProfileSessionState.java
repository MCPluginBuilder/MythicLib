package io.lumine.mythic.lib.profile;

/**
 * Different states of a player session.
 */
public enum ProfileSessionState {

    /**
     * Player just logged in and no plugin has been marked
     * as ready yet.
     * <p>
     * Possible transitions:
     * - {@link #OPENING} if any plugin is marked as ready.
     */
    CREATED,

    /**
     * Player just logged in. If a profile plugin is installed,
     * the MMO plugins will NOT load data yet so player session
     * will stay at this state.
     * <p>
     * Possible transitions:
     * - {@link #OPEN} if all MMO plugins successfully loaded their
     * data. For MythicLib the player has officially "started playing".
     * - {@link #ABORTING} if all MMO plugins successfully loaded their
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
     * In this state, MythicLib is waiting for other plugins
     * to write back their data to the database.o
     * <p>
     * Possible transitions:
     * - {@link #DEAD} when all MMO plugins have successfully
     * written back their data to the database.
     */
    CLOSING,

    /**
     * Not all MMO plugins managed to read their player data
     * before session was closed. MythicLib is waiting for
     * answers from the MMO plugins which managed to load
     * data from the database, but not the other ones.
     * <p>
     * Transitions:
     * - {@link #DEAD_EARLY} when all MMO plugins have successfully
     * written back their data to the database.
     */
    ABORTING,

    /**
     * All MMO plugins have successfully written back their
     * data to the database and the session is fully invalidated.
     * <p>
     * If the same player logs back, they can re-open a new session
     * at state {@link #OPENING}.
     * <p>
     * Possible transitions:
     * - {@link #CREATED} on session reset
     */
    DEAD,

    DEAD_EARLY;

    public boolean wasReady() {
        return this == OPEN || this == CLOSING || this == DEAD;
    }

    public boolean isClosing() {
        return this == CLOSING || this == ABORTING;
    }

    public boolean isWaiting() {
        return this == CLOSING || this == ABORTING || this == OPENING;
    }

    public boolean isDead() {
        return this == DEAD || this == DEAD_EARLY;
    }
}
