package io.lumine.mythic.lib.profile;

import fr.phoenixdevt.profiles.event.ProfileUnloadEvent;
import org.jetbrains.annotations.NotNull;

public enum SessionUpdateReason {

    /**
     * Player is still online and data is being autosaved
     * <p>
     * Technically does not require any session update
     */
    AUTOSAVE,

    /**
     * Player is leaving the server
     * <p>
     * Session closes
     */
    LOG_OUT,

    /**
     * Player is quiting a profile to either stop playing
     * or switch to another profile
     * <p>
     * Session closes, no other session opens
     */
    QUIT_PROFILE,

    /**
     * A player switches to another profile
     * <p>
     * Session closes, then another opens
     */
    SWITCH_PROFILE,

    /**
     * When a player chooses a profile and starts playing
     * <p>
     * Sessions opens
     */
    LOGIN,

    /**
     * Not specified
     */
    UNSPECIFIED;

    @NotNull
    public static SessionUpdateReason from(ProfileUnloadEvent.Reason reason) {
        switch (reason) {
            case QUIT_PROFILE:
                return QUIT_PROFILE;
            case SWITCH_PROFILE:
                return SWITCH_PROFILE;
            case LOG_OUT:
                return LOG_OUT;
            default:
                throw new IllegalArgumentException("Cannot adapt reason " + reason);
        }
    }
}
