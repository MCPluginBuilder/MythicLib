package io.lumine.mythic.lib.util;

import io.lumine.mythic.lib.util.lang3.Validate;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private final boolean persistent;
    @Nullable
    private Supplier<T> expression;
    private boolean evaluated;
    @Nullable
    private T value;

    private Lazy(@Nullable Supplier<T> expression, boolean persistent) {
        this.expression = expression;
        this.persistent = persistent;
    }

    public void flush() {
        Validate.isTrue(persistent, "Non persistent lazy value");
        value = null;
        evaluated = false;
    }

    public static <T> Lazy<T> persistent(Supplier<T> expression) {
        return new Lazy<>(expression, true);
    }

    public static <T> Lazy<T> of(Supplier<T> expression) {
        return new Lazy<>(expression, true);
    }

    @Override
    public T get() {
        if (evaluated) return value;

        Validate.notNull(expression, "Non persistent lazy value");
        value = expression.get(); // If exception happens, stops there
        evaluated = true;
        if (!persistent) expression = null;
        return value;
    }
}
