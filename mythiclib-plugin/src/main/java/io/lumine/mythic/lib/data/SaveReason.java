package io.lumine.mythic.lib.data;

import fr.phoenixdevt.profiles.event.ProfileUnloadEvent;
import org.jetbrains.annotations.NotNull;

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
    QUIT_PROFILE;

    @NotNull
    public static SaveReason from(ProfileUnloadEvent.Reason reason) {
        switch (reason) {
            case QUIT_PROFILE:
            case SWITCH_PROFILE:
                return SaveReason.QUIT_PROFILE;
            case LOG_OUT:
                return SaveReason.LOG_OUT;
            default:
                throw new IllegalArgumentException("Cannot adapt reason " + reason);
        }
    }
}
