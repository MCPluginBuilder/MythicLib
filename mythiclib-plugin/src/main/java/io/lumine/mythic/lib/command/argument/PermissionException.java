package io.lumine.mythic.lib.command.argument;

public class PermissionException extends ArgumentParseException {
    public PermissionException() {
        super("Not enough permissions");
    }
}
