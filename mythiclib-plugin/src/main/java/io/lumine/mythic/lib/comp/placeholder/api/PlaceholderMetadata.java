package io.lumine.mythic.lib.comp.placeholder.api;

public class PlaceholderMetadata<T> {
    public final T playerData;
    public final int argIndex;
    public final String placeholderInput;

    PlaceholderMetadata(T playerData, String placeholderInput, int argIndex) {
        this.playerData = playerData;
        this.argIndex = argIndex;
        this.placeholderInput = placeholderInput;
    }

    public String params() {
        return placeholderInput.substring(this.argIndex);
    }
}
