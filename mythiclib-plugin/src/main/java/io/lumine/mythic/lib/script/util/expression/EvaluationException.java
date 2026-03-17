package io.lumine.mythic.lib.script.util.expression;

public class EvaluationException extends RuntimeException {
    public EvaluationException(Exception cause) {
        super(cause);
    }

    public EvaluationException(String message, Exception cause) {
        super(message, cause);
    }

    public EvaluationException(String message) {
        super(message);
    }
}
