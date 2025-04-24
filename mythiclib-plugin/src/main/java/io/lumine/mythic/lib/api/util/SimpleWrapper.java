package io.lumine.mythic.lib.api.util;

@Deprecated
public class SimpleWrapper<T> {
    private T object;

    public SimpleWrapper() {}

    public SimpleWrapper(T object) {
        this.object = object;
    }

    public T getValue() {
        return this.object;
    }

    public void setValue(T object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return this.object.toString();
    }
}
