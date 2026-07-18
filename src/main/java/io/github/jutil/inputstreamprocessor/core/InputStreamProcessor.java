package io.github.jutil.inputstreamprocessor.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Coordinates incremental parsing of an input stream with consumption of the
 * resulting items.
 *
 * <p>The configured {@link InputParser} is responsible for interpreting the
 * input format and emitting items. The client {@link Consumer} is responsible
 * for processing each emitted item and deciding whether to handle its own
 * application failures. This processor only connects those responsibilities
 * and counts successful consumer calls; it does not catch or classify parser or
 * consumer failures.</p>
 *
 * <p>This class is immutable. It does not close supplied input streams; stream
 * ownership remains with the caller.</p>
 *
 * @param <T> the type of item processed
 */
public final class InputStreamProcessor<T> {

    private final InputParser<T> parser;

    /**
     * Creates a processor that delegates incremental item extraction to
     * {@code parser}.
     *
     * @param parser the format-specific or application-specific parser to use
     * @throws NullPointerException if {@code parser} is {@code null}
     */
    public InputStreamProcessor(InputParser<T> parser) {
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    /**
     * Parses {@code input} and passes every emitted item to {@code consumer}.
     *
     * <p>The processed count is incremented only after a call to
     * {@link Consumer#accept(Object)} returns normally. A runtime exception from
     * the consumer or an {@link IOException} from the parser propagates
     * unchanged and terminates processing. The processor does not close
     * {@code input}, including when processing terminates exceptionally.</p>
     *
     * @param input the caller-owned input stream to process
     * @param consumer the client operation to invoke for every emitted item
     * @return an immutable result containing the number of successful consumer
     *         calls
     * @throws IOException if the parser propagates an input failure
     * @throws RuntimeException if the parser or consumer propagates a runtime
     *         failure
     * @throws NullPointerException if {@code input} or {@code consumer} is
     *         {@code null}
     */
    public ProcessingResult process(
            InputStream input,
            Consumer<? super T> consumer
    ) throws IOException {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(consumer, "consumer");

        CountingConsumer<T> countingConsumer = new CountingConsumer<>(consumer);
        parser.parse(input, countingConsumer);
        return new ProcessingResult(countingConsumer.getProcessedCount());
    }
}
