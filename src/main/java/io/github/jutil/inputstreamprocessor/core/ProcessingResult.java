package io.github.jutil.inputstreamprocessor.core;

/**
 * Immutable summary of a completed input-stream processing operation.
 *
 * <p>The processed count is the number of emitted items for which the client
 * consumer returned normally. Items whose consumer call terminates by throwing
 * are not counted.</p>
 */
public final class ProcessingResult {

    private final long processedCount;

    ProcessingResult(long processedCount) {
        this.processedCount = processedCount;
    }

    /**
     * Returns the number of items successfully accepted by the client consumer.
     *
     * @return the successful consumer-call count
     */
    public long getProcessedCount() {
        return processedCount;
    }
}
