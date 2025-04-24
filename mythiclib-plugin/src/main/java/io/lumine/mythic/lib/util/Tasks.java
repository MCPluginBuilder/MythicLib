package io.lumine.mythic.lib.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Tasks {
    private static final List<Integer> ASYNC_SAFE_TASKS = new ArrayList<>();
    private static final List<Integer> SYNC_SAFE_TASKS = new ArrayList<>();

    /**
     * Time the main Bukkit thread waits before checking again
     * that this plugin's active workers have finished running.
     */
    private static final long ASYNC_TIME_OUT = 50;

    public static boolean isSafeAsync(@NotNull BukkitTask task) {
        synchronized (ASYNC_SAFE_TASKS) {
            return ASYNC_SAFE_TASKS.contains(task.getTaskId());
        }
    }

    public static boolean isSafeSync(@NotNull BukkitTask task) {
        synchronized (SYNC_SAFE_TASKS) {
            return SYNC_SAFE_TASKS.contains(task.getTaskId());
        }
    }

    public static boolean isSafe(@NotNull BukkitTask task) {
        return isSafeSync(task) || isSafeAsync(task);
    }

    /**
     * Executes safe SYNC and ASYNC pending tasks. Pending tasks are
     * delays and timers which have been scheduled for later but that
     * have not been executed yet. Among these, it is important to execute
     * the safe tasks before the server shuts down.
     *
     * @param plugin Plugin owner
     */
    public static void executePendingSafe(@NotNull Plugin plugin) {
        for (BukkitTask pending : Bukkit.getScheduler().getPendingTasks())
            if (pending.getOwner().equals(plugin) && isSafe(pending)) ((Runnable) pending).run();
    }

    /**
     * Joins current thread with active workers performing safe
     * async tasks. Safe tasks are tasks which are guaranteed to
     * finish before the server shuts down.
     *
     * @param plugin Plugin owner
     */
    public static void waitSafe(@NotNull Plugin plugin) {
        while (Bukkit.getScheduler().getActiveWorkers().stream().anyMatch(worker -> worker.getOwner().equals(plugin))) {
            try {
                Thread.sleep(ASYNC_TIME_OUT);
            } catch (InterruptedException exception) {
                // Ignore
            }
        }
    }

    /**
     * Runs a safe task in a worker which is guaranteed by MythicLib
     * to finish its work before the server turns off. This can be used
     * for very important async tasks like saving player data.
     * <p>
     * By definition, safe tasks are asynchronously as all tasks ran
     * on the server main thread are already guaranteed to complete.
     *
     * @param plugin   Plugin owning task
     * @param runnable Something to run
     */
    @NotNull
    public static CompletableFuture<Void> runSafeAsync(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            // Execute
            try {
                runnable.run();
            } catch (Throwable throwable) {
                printStackTraceSync(plugin, throwable);
            }

            // Complete future
            future.complete(null);
        });

        // Register task
        synchronized (ASYNC_SAFE_TASKS) {
            ASYNC_SAFE_TASKS.add(task.getTaskId());
        }

        return future.thenRun(() -> {
            // Unregister task
            synchronized (ASYNC_SAFE_TASKS) {
                ASYNC_SAFE_TASKS.remove((Integer) task.getTaskId());
            }
        });
    }

    private static void printStackTraceSync(@NotNull Plugin plugin, @NotNull Throwable throwable) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().info("Error caught on non-main thread:");
            throwable.printStackTrace();
        });
    }

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
        final CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {

            // Execute task
            try {
                runnable.run();
            } catch (Throwable throwable) {
                printStackTraceSync(plugin, throwable);
            }

            // Complete future
            future.complete(null);
        });
        return future;
    }

    public static void runSync(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    /**
     * Wraps a task inside a sync block to make sure the task runs
     * in sync. Handy util when working with completable futures.
     *
     * @param plugin   Plugin performing the sync task
     * @param syncTask Task to be performed sync
     * @return Runnable wrapping another runnable in a sync block.
     */
    public static <T> Consumer<T> sync(@NotNull Plugin plugin, @NotNull Consumer<T> syncTask) {
        return t -> Bukkit.getScheduler().runTask(plugin, () -> syncTask.accept(t));
    }
}
