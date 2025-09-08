package io.lumine.mythic.lib.util;


import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A safer version of CompletableFutures for Bukkit. The main
 * issue with CompletableFutures is that you cannot tell which thread
 * will be executing the consumers passed as arguments.
 * <p>
 * This implementation of futures guarantees that provided consumers
 * will be executed either on an async thread or on the main server thread.
 * <p>
 * Any exception called by an async thread will also print its stack trace
 * to the console.
 *
 * @param <T> Parameter of wrapped instance of CompletableFuture
 */
@Deprecated
public class BukkitFuture<T> {
    private final Plugin plugin;
    private final CompletableFuture<T> wrapped;

    private BukkitFuture(@NotNull Plugin plugin) {
        this(plugin, new CompletableFuture<>());
    }

    private BukkitFuture(@NotNull Plugin plugin, @NotNull CompletableFuture<T> wrapped) {
        this.plugin = plugin;
        this.wrapped = wrapped;
    }

    @NotNull
    public BukkitFuture<Void> thenSync(Consumer<T> consumer) {
        return new BukkitFuture<>(plugin, wrapped.thenAccept(Tasks.sync(plugin, consumer)));
    }

    @NotNull
    public BukkitFuture<Void> thenAsync(Consumer<T> consumer) {
        return new BukkitFuture<>(plugin, wrapped.thenAccept(t -> {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    consumer.accept(t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }));
    }

    public boolean complete(T t) {
        return wrapped.complete(t);
    }

    public static <T> BukkitFuture<T> async(@NotNull Plugin plugin, @NotNull Supplier<T> supplier) {
        final BukkitFuture<T> future = new BukkitFuture<>(plugin);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                T result = supplier.get();
                future.complete(result);
            } catch (Exception e) {
                e.printStackTrace();
                future.complete(null);
            }
        });
        return future;
    }
}
