package io.lumine.mythic.lib.util.configobject;

public class MissingArgumentException extends RuntimeException {
    public MissingArgumentException(String... aliases) {
        super("Expected one of [" + String.join(", ", aliases) + "]");
    }
}
