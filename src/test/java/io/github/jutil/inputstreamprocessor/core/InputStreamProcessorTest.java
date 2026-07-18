package io.github.jutil.inputstreamprocessor.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class InputStreamProcessorTest {

    @Test
    void emittedItemsReachConsumerInOrder() throws IOException {
        InputStreamProcessor<String> processor = processorEmitting("first", "second", "third");
        List<String> consumed = new ArrayList<>();

        processor.process(emptyInput(), consumed::add);

        assertEquals(Arrays.asList("first", "second", "third"), consumed);
    }

    @Test
    void resultContainsNumberOfSuccessfulConsumerCalls() throws IOException {
        InputStreamProcessor<String> processor = processorEmitting("first", "second", "third");

        ProcessingResult result = processor.process(emptyInput(), item -> {
        });

        assertEquals(3L, result.getProcessedCount());
    }

    @Test
    void zeroEmittedItemsProduceZeroCount() throws IOException {
        InputStreamProcessor<String> processor =
                new InputStreamProcessor<>((input, emitter) -> {
                });

        ProcessingResult result = processor.process(emptyInput(), item -> {
        });

        assertEquals(0L, result.getProcessedCount());
    }

    @Test
    void consumerRuntimeExceptionPropagatesUnchanged() {
        RuntimeException failure = new IllegalStateException("consumer failed");
        InputStreamProcessor<String> processor = processorEmitting("item");

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> processor.process(emptyInput(), item -> {
                    throw failure;
                })
        );

        assertSame(failure, thrown);
    }

    @Test
    void failedConsumerCallIsNotCounted() {
        CountingConsumer<String> consumer = new CountingConsumer<>(item -> {
            if ("failed".equals(item)) {
                throw new IllegalStateException("consumer failed");
            }
        });

        consumer.accept("successful");
        assertThrows(IllegalStateException.class, () -> consumer.accept("failed"));

        assertEquals(1L, consumer.getProcessedCount());
    }

    @Test
    void processingStopsWhenConsumerThrows() {
        InputStreamProcessor<String> processor = processorEmitting("first", "second", "third");
        List<String> attempted = new ArrayList<>();

        assertThrows(
                IllegalStateException.class,
                () -> processor.process(emptyInput(), item -> {
                    attempted.add(item);
                    if ("second".equals(item)) {
                        throw new IllegalStateException("stop");
                    }
                })
        );

        assertEquals(Arrays.asList("first", "second"), attempted);
    }

    @Test
    void parserIOExceptionPropagatesUnchanged() {
        IOException failure = new IOException("parser failed");
        InputStreamProcessor<String> processor =
                new InputStreamProcessor<>((input, emitter) -> {
                    throw failure;
                });

        IOException thrown = assertThrows(
                IOException.class,
                () -> processor.process(emptyInput(), item -> {
                })
        );

        assertSame(failure, thrown);
    }

    @Test
    void processorDoesNotCloseSuppliedInputStream() throws IOException {
        CloseTrackingInputStream input =
                new CloseTrackingInputStream("content".getBytes(StandardCharsets.UTF_8));
        InputStreamProcessor<Integer> processor =
                new InputStreamProcessor<>((stream, emitter) -> {
                    while (stream.read() != -1) {
                        // Consume the stream without closing it.
                    }
                });

        processor.process(input, item -> {
        });

        assertFalse(input.isClosed());
    }

    @Test
    void constructorRejectsNullParser() {
        assertThrows(NullPointerException.class, () -> new InputStreamProcessor<>(null));
    }

    @Test
    void processRejectsNullInput() {
        InputStreamProcessor<String> processor = processorEmitting("item");

        assertThrows(NullPointerException.class, () -> processor.process(null, item -> {
        }));
    }

    @Test
    void processRejectsNullConsumer() {
        InputStreamProcessor<String> processor = processorEmitting("item");

        assertThrows(NullPointerException.class, () -> processor.process(emptyInput(), null));
    }

    @Test
    void consumerThatHandlesApplicationFailureAllowsProcessingToContinue() throws IOException {
        InputStreamProcessor<String> processor = processorEmitting("first", "second", "third");
        List<String> successfullyHandled = new ArrayList<>();

        ProcessingResult result = processor.process(emptyInput(), item -> {
            try {
                performClientWork(item);
                successfullyHandled.add(item);
            } catch (ApplicationException ignored) {
                // The client owns its application failure policy.
            }
        });

        assertEquals(Arrays.asList("first", "third"), successfullyHandled);
        assertEquals(3L, result.getProcessedCount());
    }

    @Test
    void parserThatHandlesRecoverableFailureCanEmitLaterItems() throws IOException {
        InputParser<String> parser = (input, emitter) -> {
            emitter.accept("before");
            try {
                parseRecoverableItem();
            } catch (RecoverableParserException ignored) {
                // The parser owns its recoverable failure policy.
            }
            emitter.accept("after");
        };
        InputStreamProcessor<String> processor = new InputStreamProcessor<>(parser);
        List<String> consumed = new ArrayList<>();

        ProcessingResult result = processor.process(emptyInput(), consumed::add);

        assertEquals(Arrays.asList("before", "after"), consumed);
        assertEquals(2L, result.getProcessedCount());
    }

    private static InputStreamProcessor<String> processorEmitting(String... items) {
        List<String> emitted = Collections.unmodifiableList(Arrays.asList(items));
        return new InputStreamProcessor<>((input, emitter) -> {
            for (String item : emitted) {
                emitter.accept(item);
            }
        });
    }

    private static InputStream emptyInput() {
        return new ByteArrayInputStream(new byte[0]);
    }

    private static void performClientWork(String item) {
        if ("second".equals(item)) {
            throw new ApplicationException();
        }
    }

    private static void parseRecoverableItem() {
        throw new RecoverableParserException();
    }

    private static final class ApplicationException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    private static final class RecoverableParserException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    private static final class CloseTrackingInputStream extends ByteArrayInputStream {

        private final AtomicBoolean closed = new AtomicBoolean();

        private CloseTrackingInputStream(byte[] content) {
            super(content);
        }

        @Override
        public void close() throws IOException {
            closed.set(true);
            super.close();
        }

        private boolean isClosed() {
            return closed.get();
        }
    }
}
