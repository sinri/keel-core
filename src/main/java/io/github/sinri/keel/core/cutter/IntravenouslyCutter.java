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

    void stopHere();

    Future<Void> waitForAllHandled();
}
