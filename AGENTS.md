# AGENTS.md

## Repository purpose

This repository publishes `io.github.j-util:inputstream-processor-core`: a tiny, dependency-free Java core for incremental item processing from `InputStream`.

Treat `README.md`, public Javadocs, tests, and Maven metadata as the user-facing contract. Keep durable agent workflow guidance here, but document library behavior in the public documentation as well.

## j-util library standards

- Preserve Java 8 compatibility. Do not use language features or JDK APIs introduced after Java 8.
- Use the Maven wrapper for builds and verification: `./mvnw verify`.
- Keep public APIs minimal, intentional, documented, and backward-compatible.
- Add or update tests for every behavioral change and regression fix.
- Add Javadocs for public types and members. Update `README.md` and `CHANGELOG.md` when public behavior changes.
- Keep Maven Central metadata, the Apache-2.0 license, source attachment, and Javadoc attachment valid.
- Do not make performance claims without reproducible evidence.
- Do not change Maven coordinates, the Java baseline, licensing, or publishing configuration unless explicitly requested.
- Never publish, sign, tag, or create a release unless explicitly requested.

## Core-specific boundaries

- Keep the core format-neutral and protocol-neutral.
- Preserve the no-third-party-runtime-dependencies guarantee.
- Process items incrementally; do not materialize the complete input or all parsed items in memory.
- Keep V1 blocking, synchronous, and item-oriented.
- Keep parsing or framing responsibilities separate from item-consumption orchestration.
- Do not add HTTP, Jackson, CSV, XML, reactive, parallel-processing, or format-specific behavior to this core artifact without an explicit scope decision. Such integrations should normally live in separate artifacts.
- Do not swallow parser or consumer failures. Preserve their original cause when propagation requires adaptation.
- Avoid speculative abstractions and extension points. Add them only for an established use case.

## Working process

1. Inspect the current public API, tests, `README.md`, and `pom.xml` before changing behavior.
2. Make the smallest coherent change that satisfies the request.
3. Preserve unrelated user changes.
4. For code or build changes, run `./mvnw verify`.
5. Report what changed, the verification performed, and any remaining compatibility or documentation concern.
