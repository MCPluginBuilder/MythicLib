package io.lumine.mythic.lib.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Tasks {

    /**
     * Prefer using this method over {@link CompletableFuture#runAsync(Runnable)}
     * as using Bukkit scheduler to manage other threads is always preferable
     * over the default Java concurrent package. This notably fixes MMOCore#971
     * and seems to introduce less concurrency issues.
     * <p>
     * Unlike the Java method, this method does print out stack traces in the
     * server console in case of exceptions or errors.
     *
     * @param runnable Task to execute async
     * @return Future that will be completed inside an async Bukkit task
     */
    @NotNull
    public static CompletableFuture<Void> runAsync(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        final var future = new CompletableFuture<Void>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {

            // Execute task
            try {
                runnable.run();
            } catch (Throwable throwable) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().info("Error caught on async thread:");
                    throwable.printStackTrace();
                });
            }

            // Complete future
            future.complete(null);
        });
        return future;
    }

    /**
     * Runs the provided runnable on the main server thread. If the calling
     * thread is the server thread, it is executed instantly. Therefore, this
     * method does not guarantee that the provided runnable will run on the
     * next server tick.
     */
    public static void runSync(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        if (Bukkit.isPrimaryThread()) runnable.run();
        else Bukkit.getScheduler().runTask(plugin, runnable);
    }

    /**
     * Wraps a task inside a sync block to make sure the task runs
     * in sync. Handy util when working with completable futures.
     *
     * @param plugin   Plugin performing the sync task
     * @param syncTask Task to be performed sync
     * @return Runnable wrapping another runnable in a sync block.
     */
    @NotNull
    public static <T> Consumer<T> sync(@NotNull Plugin plugin, @NotNull Consumer<T> syncTask) {
        return t -> runSync(plugin, () -> syncTask.accept(t));
    }

    /**
     * Wraps a task inside a sync block to make sure the task runs
     * in sync. Handy util when working with completable futures.
     *
     * @param plugin   Plugin performing the sync task
     * @param syncTask Task to be performed sync
     * @return Runnable wrapping another runnable in a sync block.
     */
    @NotNull
    public static Runnable sync(@NotNull Plugin plugin, @NotNull Runnable syncTask) {
        return () -> runSync(plugin, syncTask);
    }
}
