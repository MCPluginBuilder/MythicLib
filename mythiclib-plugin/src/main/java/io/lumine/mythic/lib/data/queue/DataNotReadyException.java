package io.lumine.mythic.lib.data.queue;

public class DataNotReadyException extends RuntimeException {
    public DataNotReadyException() {
        super("Data not ready yet");
    }
}
