package io.lumine.mythic.lib.command;

public class CommandException extends IllegalArgumentException {
    public CommandException(String message, Exception cause) {
        super(message, cause);
    }

    public CommandException(String message) {
        super(message);
    }
}
