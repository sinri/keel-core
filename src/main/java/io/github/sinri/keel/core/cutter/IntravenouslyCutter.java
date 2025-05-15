package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.core.servant.intravenous.KeelIntravenous;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

/**
 * Create one instance, call {@link IntravenouslyCutter#acceptFromStream(Buffer)} for each piece from stream, call
 * {@link IntravenouslyCutter#stopHere()} when no more data from stream, and then call
 * {@link IntravenouslyCutter#waitForAllHandled()} to a completion.
 *
 * @since 4.0.11
 */
public interface IntravenouslyCutter<T> {
    KeelIntravenous.SingleDropProcessor<T> getSingleDropProcessor();

    void acceptFromStream(Buffer t);

    /**
     * Notify the cutter to stop accept chunks, handle out the existed buffer, and then stop.
     * Use this method, means no throwable met during buffer receiving and accepting.
     * As of 4.0.12, rely on {@link IntravenouslyCutter#stopHere(Throwable)}.
     */
    default void stopHere() {
        stopHere(null);
    }

    /**
     * Notify the cutter to stop accept chunks, handle out the existed buffer, and then stop.
     * If the throwable is set as a Non-Null value, such as network error or generator failure, it would result in a
     * failure future be returned in {@link IntravenouslyCutter#waitForAllHandled()}.
     *
     * @param throwable Any exception met during cutter working. It is nullable.
     * @since 4.0.12
     */
    void stopHere(Throwable throwable);

    /**
     * @return a future as cutter finished work and all given buffer handled; it would be a failure future when stop
     *         with throwable.
     */
    Future<Void> waitForAllHandled();

    /**
     * The exception that shows the cutter is still running, but timeout occurs so cutter stopped with this exception.
     *
     * @since 4.0.12
     */
    final class Timeout extends Exception {
        public Timeout() {
            super("This IntravenouslyCutter instance met timeout");
        }
    }
}
