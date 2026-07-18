# InputStream Processor Core

A tiny, format-neutral Java core for incremental item processing from
`InputStream`. The core has no third-party runtime dependencies.

Maven coordinate: `io.github.j-util:inputstream-processor-core`

## Responsibilities

The API separates three responsibilities:

* An `InputParser<T>` interprets an input format and incrementally emits logical
  items.
* An `InputStreamProcessor<T>` connects the parser to the client consumer and
  counts consumer calls that return successfully.
* A client `Consumer<? super T>` handles each item and owns the application's
  failure policy.

The core does not catch or classify parser or consumer failures. If a consumer
handles an application failure itself, processing can continue. A runtime
exception allowed out of the consumer terminates processing. Likewise, a parser
can handle its own recoverable failures, while an `IOException` allowed out of
the parser terminates processing.

## JDK-only example

```java
import io.github.jutil.inputstreamprocessor.core.InputParser;
import io.github.jutil.inputstreamprocessor.core.InputStreamProcessor;
import io.github.jutil.inputstreamprocessor.core.ProcessingResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

Path path = Paths.get("input.txt");

InputParser<String> parser = (input, emit) -> {
    BufferedReader reader = new BufferedReader(
            new InputStreamReader(input, StandardCharsets.UTF_8)
    );
    String line;

    while ((line = reader.readLine()) != null) {
        emit.accept(line);
    }
};

InputStreamProcessor<String> processor = new InputStreamProcessor<>(parser);

try (InputStream input = Files.newInputStream(path)) {
    ProcessingResult result = processor.process(input, System.out::println);
    System.out.println("Processed: " + result.getProcessedCount());
}
```

The caller owns the supplied `InputStream`. Neither the processor nor a parser
implementation should close it; the caller should close it after processing, as
shown above. In particular, the example intentionally does not close the
`BufferedReader`, because doing so would also close the caller-owned stream.

JSON, CSV, and XML integrations are intentionally outside this dependency-free
core. Applications can implement `InputParser<T>` using whichever format
library they choose.
