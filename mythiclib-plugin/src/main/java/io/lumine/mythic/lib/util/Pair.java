package io.lumine.mythic.lib.util;

import org.jetbrains.annotations.Nullable;

public class Pair<L, R> {
    private final @Nullable L left;
    private final @Nullable R right;

    private Pair(@Nullable L left, @Nullable R right) {
        this.left = left;
        this.right = right;
    }

    public @Nullable L getLeft() {
        return this.left;
    }

    @Deprecated
    public L getKey() {
        return this.left;
    }

    public @Nullable R getRight() {
        return this.right;
    }

    @Deprecated
    public R getValue() {
        return this.right;
    }

    public static <L, R> Pair<L, R> of(@Nullable L left, @Nullable R right) {
        return new Pair<>(left, right);
    }
}
