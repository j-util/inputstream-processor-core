package io.github.jutil.inputstreamprocessor.core;

import java.util.function.Consumer;

final class CountingConsumer<T> implements Consumer<T> {

    private final Consumer<? super T> delegate;
    private long processedCount;

    CountingConsumer(Consumer<? super T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void accept(T item) {
        delegate.accept(item);
        processedCount++;
    }

    long getProcessedCount() {
        return processedCount;
    }
}
