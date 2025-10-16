package io.lumine.mythic.lib.data;

import io.lumine.mythic.lib.util.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DataLoadResult {

    public final boolean empty, sync;
    public final Type type;

    public DataLoadResult(@NotNull Type type) {
        this(type, false, true);

        Validate.isTrue(type != Type.SUCCESS, "Must specify for SUCCESS");
    }

    public DataLoadResult(@NotNull Type type, boolean empty, boolean sync) {
        this.empty = empty;
        this.sync = sync;
        this.type = Objects.requireNonNull(type);
    }

    public static enum Type {

        /**
         * Player data loaded successfully
         */
        SUCCESS,

        /**
         * Data was found but `is_saved` is still set to 0
         */
        NOT_SYNC,

        /**
         * Player went offline, data fetching stopped
         */
        OFFLINE_PLAYER,

        /**
         * Failure to load player data due to SQL connection closed
         * or out-of-memory errors.
         */
        FAILURE,

        /**
         * Failure to load player data which is not treated as
         * a failure and does not increment the fail count
         */
        TEMPO
    }
}
