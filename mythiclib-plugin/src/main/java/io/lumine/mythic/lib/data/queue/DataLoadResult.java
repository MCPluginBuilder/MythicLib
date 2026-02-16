package io.lumine.mythic.lib.data.queue;

public class DataLoadResult {
    private final boolean empty, sync;

    public DataLoadResult(boolean empty, boolean sync) {
        this.empty = empty;
        this.sync = sync;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isSync() {
        return sync;
    }
}
