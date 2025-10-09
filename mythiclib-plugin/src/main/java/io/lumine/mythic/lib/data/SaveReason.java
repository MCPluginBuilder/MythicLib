package io.lumine.mythic.lib.data;

public enum SaveReason {

    /**
     * Player is still online and data is being autosaved
     */
    AUTOSAVE,

    /**
     * Player is leaving the server
     */
    LOG_OUT,

    /**
     * Player is quiting a profile to either stop playing
     * or switch to another profile
     */
    QUIT_PROFILE
}
