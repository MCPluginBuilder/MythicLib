package io.lumine.mythic.lib.script.util.expression;

public class EvaluationException extends RuntimeException {
    public EvaluationException(Throwable cause) {
        super(cause);
    }

    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvaluationException(String message) {
        super(message);
    }
}
