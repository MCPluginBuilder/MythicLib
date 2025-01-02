package io.lumine.mythic.lib.util.lang3;

public class NotImplementedException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Code not implemented";

    public NotImplementedException() {
        super(DEFAULT_MESSAGE);
    }

    public NotImplementedException(String message) {
        super(message);
    }
}
