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
     * Player is switching live to another profile
     */
    QUIT_PROFILE
}
