package io.lumine.mythic.lib.data.queue;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.data.Database;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.module.MMOPlugin;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public abstract class DataQueue<H extends SynchronizedDataHolder> implements Runnable {

    /**
     * Plugin owning the data queue
     */
    protected final MMOPlugin plugin;

    /**
     * Database (previously known as data handler/data source)
     */
    protected final Database<H, ?> database;

    /**
     * Player data manager
     */
    protected final SynchronizedDataManager<H, ?> manager;

    /**
     * Thread safe blocking queue which stores records to be processed
     */
    protected final LinkedBlockingQueue<QueueRecord> recordQueue = new LinkedBlockingQueue<>();

    protected final int maxTries;

    private final AtomicReference<Thread> workerThread = new AtomicReference<>(null);
    protected volatile boolean shutdownRequested;

    @Nullable
    private UUID lastProcessedId = null;

    protected static final long WAIT_TIME = 1000;

    public DataQueue(@NotNull SynchronizedDataManager<H, ?> manager) {
        this.plugin = manager.getOwningPlugin();
        this.database = manager.getDatabase();
        this.manager = manager;

        // Cache config option
        maxTries = MythicLib.plugin.getMMOConfig().maxSyncTries;
    }

    public boolean isRunning() {
        return workerThread.get() != null;
    }

    @Override
    public void run() {

        // Atomically set worker thread
        Validate.isTrue(workerThread.compareAndSet(null, Thread.currentThread()), "DataQueue is already running in another thread");

        try {
            while (!shutdownRequested || !recordQueue.isEmpty()) {

                // Wait until queue not empty
                QueueRecord record;
                try {
                    record = recordQueue.take();
                } catch (InterruptedException ignored) {
                    continue;
                }

                // Prevent spinning
                if (record.effectiveId.equals(lastProcessedId) && !record.available()) {
                    enqueue(record);
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException ignored) {
                    }
                    continue;
                }

                lastProcessedId = record.effectiveId;
                processRecord(record);
            }
        } catch (Exception | LinkageError throwable) {
            throwable.printStackTrace();
        } finally {
            // Access termination thread after setting worker thread to null
            workerThread.set(null);
        }
    }

    /**
     * Ran from worker thread
     */
    protected abstract void processRecord(QueueRecord record);

    /**
     * Ran from server thread
     */
    protected void enqueue(QueueRecord record) {
        Validate.isTrue(this.recordQueue.offer(record), "Internal error: linked queue is full?");
    }

    public void end() {
        final var worker = Objects.requireNonNull(workerThread.get(), "DataQueue is not running");
        Validate.isTrue(!shutdownRequested, "Shutdown already scheduled");

        shutdownRequested = true;
        worker.interrupt();
    }

    private static final long MESSAGE_INTERVAL = 3000;

    public void sleepUntilCompletion() {
        Validate.isTrue(Bukkit.isPrimaryThread(), "Must be called from the main thread");

        long lastMessage = System.currentTimeMillis();
        while (isRunning()) try {
            if (System.currentTimeMillis() > lastMessage + MESSAGE_INTERVAL) {
                lastMessage = System.currentTimeMillis();
                this.plugin.getLogger().log(Level.INFO, "Waiting " + getClass().getSimpleName() + " to process " + this.recordQueue.size() + " records");
            }
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
    }

    public abstract class QueueRecord {
        public final int tryCount;
        public final H playerData;
        public final UUID effectiveId;
        public final CompletableFuture<Void> future;
        public final long availableAt;

        QueueRecord(H playerData, UUID effectiveId, CompletableFuture<Void> future, long availableAt, int tryCount) {
            this.playerData = playerData;
            this.effectiveId = effectiveId;
            this.future = future;
            this.availableAt = availableAt;
            this.tryCount = tryCount;
        }

        public boolean hitThreshold() {
            return this.tryCount >= maxTries;
        }

        @NotNull
        public abstract QueueRecord nextTry();

        public boolean available() {
            return System.currentTimeMillis() > availableAt;
        }
    }
}
