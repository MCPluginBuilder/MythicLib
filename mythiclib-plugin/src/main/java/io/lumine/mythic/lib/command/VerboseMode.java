package io.lumine.mythic.lib.command;

/**
 * These options only apply to admin commands. Since they are not
 * seen by players, admin commands are not translatable, and these
 * options provide a way to edit the verbosity of admin command feedback.
 */
public enum VerboseMode {

    /**
     * Send all command feedback. Default value.
     */
    ALL,

    /**
     * Only send feedback when command sender is a player
     */
    PLAYER,

    /**
     * Only send feedback when command sender is the console
     */
    CONSOLE,

    /**
     * Redirect all command feedback to server console,
     * if the command sender is a player.
     */
    REDIRECT_TO_CONSOLE,

    /**
     * Never send command feedback
     */
    NONE
}
