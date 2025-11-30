package io.lumine.mythic.lib.script.util;

public class ScriptException extends RuntimeException {
    public ScriptException(String message, Exception exception) {
        super(message, exception);
    }

    public ScriptException(Exception exception) {
        super(exception);
    }

    public ScriptException(String message) {
        super(message);
    }
}
