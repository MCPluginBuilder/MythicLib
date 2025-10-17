package io.lumine.mythic.lib.data.queue;

import io.lumine.mythic.lib.data.Database;
import io.lumine.mythic.lib.data.SynchronizedDataHolder;
import io.lumine.mythic.lib.data.SynchronizedDataManager;
import io.lumine.mythic.lib.module.MMOPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class DataQueue<H extends SynchronizedDataHolder> extends Thread {

    /**
     * Plugin owning the data queue
     */
    protected final MMOPlugin owning;

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
    private final Queue<QueueRecord> queue = new ConcurrentLinkedQueue<>();

    private boolean stopIfEmpty = false, stopped;

    public DataQueue(@NotNull SynchronizedDataManager<H, ?> manager) {
        this.owning = manager.getOwningPlugin();
        this.database = manager.getDatabase();
        this.manager = manager;
    }

    public boolean isStopped() {
        return stopped;
    }

    @Override
    public void run() {

        boolean active = false;
        while (true) {
            while (queue.isEmpty()) {

                // Stop thread only if queue is empty
                if (stopIfEmpty) {
                    stopped = true;
                    return;
                }

                synchronized (this) {
                    try {
                        active = false;
                        wait();
                    } catch (final InterruptedException e) {
                        this.owning.getLogger().warning(getClass().getSimpleName() + " got interrupted!");
                    }
                }
            }

            // Refresh connection if needed
            if (!active) {
                while (!this.database.refreshConnection()) {
                    this.owning.getLogger().warning("Failed to re-establish connection with the database! Trying again in 1s...");
                    try {
                        sleep(1000);
                    } catch (final InterruptedException e) {
                        this.owning.getLogger().warning(getClass().getSimpleName() + " got interrupted!");
                    }
                }
                active = true;
            }

            // Process record
            final var rec = queue.poll();
            this.processRecord(rec);
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
            this.queue.add(record);
            notify();
        }
    }

    public void end() {
        synchronized (this) {
            stopIfEmpty = true;
            notifyAll();
        }
    }

    public class QueueRecord {
        public final H playerData;
        public final CompletableFuture<Void> future = new CompletableFuture<>();

        QueueRecord(H playerData) {
            this.playerData = playerData;
        }
    }
}
