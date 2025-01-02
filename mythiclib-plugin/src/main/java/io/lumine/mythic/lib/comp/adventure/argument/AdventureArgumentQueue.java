package io.lumine.mythic.lib.comp.adventure.argument;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * mythiclib
 * 30/11/2022
 *
 * @author Roch Blondiaux (Kiwix).
 * <p>
 * This class is reponsible holding the arguments in a linked queue.
 */
public class AdventureArgumentQueue {

    private final List<AdventureArgument> args;
    private int ptr = 0;

    public AdventureArgumentQueue(@NotNull List<AdventureArgument> args) {
        this.args = args;
    }

    /**
     * Pop an argument, throwing an exception if no argument was present.
     *
     * <p>After an invocation of {@code pop()}, the internal argument pointer will be advanced to the next argument.</p>
     *
     * @return the popped argument
     */
    public @NotNull AdventureArgument pop() {
        if (!this.hasNext())
            throw new IllegalStateException("Missing argument for this tag!");
        return this.args.get(this.ptr++);
    }

    /**
     * Pop an argument, throwing an exception if no argument was present.
     *
     * <p>After an invocation of {@code popOr()}, the internal argument pointer will be advanced to the next argument.</p>
     *
     * @param errorMessage the error to throw if the argument is not present
     * @return the popped argument
     * @since 4.10.0
     */
    public @NotNull AdventureArgument popOr(final @NotNull String errorMessage) {
        if (!this.hasNext())
            throw new IllegalArgumentException(errorMessage);
        return this.args.get(this.ptr++);
    }

    /**
     * Pop an argument, throwing an exception if no argument was present.
     *
     * <p>After an invocation of {@code popOr()}, the internal argument pointer will be advanced to the next argument.</p>
     *
     * @param errorMessage the error to throw if the argument is not present
     * @return the popped argument
     * @since 4.10.0
     */
    public @NotNull AdventureArgument popOr(final @NotNull Supplier<String> errorMessage) {
        if (!this.hasNext())
            throw new IllegalArgumentException(errorMessage.get());
        return this.args.get(this.ptr++);
    }

    /**
     * Peek at the next argument without advancing the iteration pointer.
     *
     * @return the next argument, if any is available.
     * @since 4.10.0
     */
    public @NotNull AdventureArgument peek() {
        return this.hasNext() ? this.args.get(this.ptr) : null;
    }

    /**
     * Get whether another argument is available to be popped.
     *
     * @return whether another argument is available
     */
    public boolean hasNext() {
        return this.ptr < this.args.size();
    }

    /**
     * Reset index to the beginning, to begin another attempt.
     */
    public void reset() {
        this.ptr = 0;
    }

    @Override
    public String toString() {
        return this.args.toString();
    }

}
