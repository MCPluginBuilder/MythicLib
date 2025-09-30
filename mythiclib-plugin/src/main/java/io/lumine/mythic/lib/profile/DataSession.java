package io.lumine.mythic.lib.profile;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * Multi threading implementation because it is notably used by SQL data
 * loaders, which operate on separate threads.
 */
public class DataSession {
    private final ProfileSession profileSession;

    private boolean wasActive;
    private boolean alive = true;

    public DataSession(ProfileSession profileSession) {
        this.profileSession = profileSession;
    }

    @NotNull
    public ProfileSession getParent() {
        return profileSession;
    }

    public synchronized boolean isAlive() {
        return alive;
    }

    public synchronized boolean hasBeenActive() {
        return wasActive;
    }

    public synchronized void markActive() {
        Validate.isTrue(!wasActive, "Data session already active");
        Validate.isTrue(alive, "Data session is dead");

        this.wasActive = true;
    }

    public synchronized void markDead() {
        Validate.isTrue(alive, "Data session already dead");

        this.alive = false;
    }
}
