package io.lumine.mythic.lib.module;

import io.lumine.mythic.lib.util.annotation.NotUsed;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

// TODO what is this for
@NotUsed
public class ModuleWrapper<T extends Module> {
    private final Supplier<T> supplier;

    @Nullable
    private T current;

    private ModuleWrapper(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public void initialize() {
        Validate.isTrue(current == null, "Module is already open");

        current = supplier.get();
        // TODO ?
    }

    public void close() {
        Validate.notNull(current, "Module is closed");
    }

    @NotNull
    public static <T extends Module> ModuleWrapper<T> from(@NotNull Supplier<T> supplier) {
        return new ModuleWrapper<>(supplier);
    }
}
