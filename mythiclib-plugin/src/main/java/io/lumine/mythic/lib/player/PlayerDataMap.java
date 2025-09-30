package io.lumine.mythic.lib.player;

public abstract class PlayerDataMap {

    protected boolean sessionOpen = false;

    public void openSession() {
        if (sessionOpen) throw new IllegalStateException("Session already open");

        sessionOpen = true;
        onSessionOpen();
    }

    public void closeSession() {
        if (!sessionOpen) throw new IllegalStateException("Session not open");

        sessionOpen = false;
        onSessionClose();
    }

    protected void onSessionOpen() {
        // nothing
    }

    protected void onSessionClose() {
        // nothing
    }
}
