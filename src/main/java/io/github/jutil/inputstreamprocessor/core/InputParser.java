package io.github.jutil.inputstreamprocessor.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Incrementally extracts logical items from an {@link InputStream}.
 *
 * <p>An implementation defines the input format and emits each item through the
 * supplied emitter as soon as it is available. This interface deliberately does
 * not prescribe a format, buffering strategy, or error-recovery policy. A parser
 * may handle recoverable failures itself and continue emitting items. Any
 * {@link IOException} it propagates terminates processing unchanged.</p>
 *
 * <p>The V1 execution contract is synchronous and blocking. Implementations
 * must invoke the supplied emitter synchronously during
 * {@link #parse(InputStream, Consumer)} and must complete all emission before
 * that method returns. They must not retain the emitter after the method
 * returns. Asynchronous emission is outside the core V1 contract.</p>
 *
 * <p>The input stream remains owned by the caller. Implementations must not
 * close it or retain it after
 * {@link #parse(InputStream, Consumer)} returns.</p>
 *
 * @param <T> the type of logical item emitted by the parser
 */
@FunctionalInterface
public interface InputParser<T> {

    /**
     * Parses items from {@code input} and passes each one to {@code emitter}.
     *
     * <p>The implementation must invoke {@code emitter} synchronously during
     * this method and must not retain {@code emitter} or {@code input} after
     * this method returns. The emitter invokes the client consumer
     * synchronously. If that consumer propagates a runtime exception, the
     * exception passes through this method and processing terminates. Parser
     * implementations may decide which of their own recoverable failures to
     * handle, but must not close {@code input}.</p>
     *
     * @param input the caller-owned stream to parse
     * @param emitter the consumer to receive incrementally parsed items
     * @throws IOException if parsing cannot continue because of an input failure
     */
    void parse(InputStream input, Consumer<? super T> emitter) throws IOException;
}
