package io.lumine.mythic.lib.data;

import io.lumine.mythic.lib.profile.SessionUpdateReason;

@Deprecated

public enum SaveReason {

    /**
     * Player is still online and data is being autosaved
     */
    @Deprecated
    AUTOSAVE,

    /**
     * Player is leaving the server
     */
    @Deprecated
    LOG_OUT,

    /**
     * Player is quiting a profile to either stop playing
     * or switch to another profile
     */
    @Deprecated
    QUIT_PROFILE;

    @Deprecated
    public SessionUpdateReason adapt() {
        switch (this) {
            case AUTOSAVE:
                return SessionUpdateReason.AUTOSAVE;
            case LOG_OUT:
                return SessionUpdateReason.LOG_OUT;
            case QUIT_PROFILE:
                return SessionUpdateReason.QUIT_PROFILE;
        }
        throw new IllegalArgumentException("Invalid reason " + this);
    }
}