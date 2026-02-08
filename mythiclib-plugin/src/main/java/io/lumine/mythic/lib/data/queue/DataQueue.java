package io.lumine.mythic.lib.data.queue;

import io.lumine.mythic.lib.data.Database;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.module.MMOPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    protected final Queue<QueueRecord> recordQueue = new ConcurrentLinkedQueue<>();

    private boolean stopIfEmpty = false, running = false;
    @Nullable
    private UUID lastProcessedId = null;

    protected static final long WAIT_TIME = 1000;

    public DataQueue(@NotNull SynchronizedDataManager<H, ?> manager) {
        this.plugin = manager.getOwningPlugin();
        this.database = manager.getDatabase();
        this.manager = manager;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {

        try {
            running = true;
            whileTrue:
            while (true) {

                // Wait until queue not empty
                while (recordQueue.isEmpty()) {

                    // Stop thread only if queue is empty
                    if (stopIfEmpty) break whileTrue;

                    waitFor(0);
                }

                // Pop record
                final var record = Objects.requireNonNull(recordQueue.poll());

                // Prevent flooding the database with requests for the same unavailable records
                if (record.effectiveId.equals(lastProcessedId) && !record.available()) {
                    enqueue(record);
                    waitFor(WAIT_TIME / 2);
                    continue;
                }

                lastProcessedId = record.effectiveId;
                processRecord(record);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            running = false;
        }
    }

    private void waitFor(long waitTime) {
        synchronized (this) {
            try {
                wait(waitTime);
            } catch (InterruptedException e) {
                this.plugin.getLogger().warning(getClass().getSimpleName() + " got interrupted: " + e.getMessage());
            }
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
        synchronized (this) {
            this.recordQueue.add(record);
            notify();
        }
    }

    public void end() {
        synchronized (this) {
            stopIfEmpty = true;
            notifyAll();
        }
    }

    private static final long MESSAGE_INTERVAL = 3000;

    public void sleepUntilCompletion() {
        long lastMessage = System.currentTimeMillis();
        while (running) try {
            if (System.currentTimeMillis() > lastMessage + MESSAGE_INTERVAL) {
                lastMessage = System.currentTimeMillis();
                this.plugin.getLogger().log(Level.INFO, "Waiting " + getClass().getSimpleName() + " to process " + this.recordQueue.size() + " records");
            }
            Thread.sleep(100);
        } catch (InterruptedException e) {
            this.plugin.getLogger().warning(getClass().getSimpleName() + " got interrupted!");
        }
    }

    public class QueueRecord {
        public final H playerData;
        public final UUID effectiveId;
        public final CompletableFuture<Void> future;
        public final long availableAt;

        QueueRecord(H playerData, UUID effectiveId, CompletableFuture<Void> future, long availableAt) {
            this.playerData = playerData;
            this.effectiveId = effectiveId;
            this.future = future;
            this.availableAt = availableAt;
        }

        boolean available() {
            return System.currentTimeMillis() > availableAt;
        }
    }
}
