package io.lumine.mythic.lib.util;

@FunctionalInterface
public interface TriFunction<A, B, C, R> {
    R apply(A var1, B var2, C var3);
}