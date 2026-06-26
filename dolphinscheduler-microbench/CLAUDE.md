# CLAUDE.md — dolphinscheduler-microbench

JMH (Java Microbenchmark Harness) micro-benchmarks. Used to measure RPC throughput and enum / hot-path utility performance. **Not** run as part of the regular build.

## Main package

`org.apache.dolphinscheduler.microbench`

## Key sub-packages / classes

- `microbench.base.AbstractBaseBenchmark` — JMH `@Setup`/`@TearDown` plumbing. 5 warmup + 10 measurement iterations by default. Runs JMH via `Runner` from a JUnit `@Test` method (so the JMH launch is triggered by `mvn test` when you opt in).
- `microbench.rpc.RpcBenchMarkTest` — Netty-based RPC ping/pong benchmarks, exercises `dolphinscheduler-extract-base`.
- `microbench.common.EnumBenchMark` — enum lookup / switch benchmarks.

## How to run

```
# Build the uber-jar
./mvnw -pl dolphinscheduler-microbench -am package

# Run (Main-Class: org.openjdk.jmh.Main)
java -jar dolphinscheduler-microbench/target/benchmarks.jar [regex]
```

Or run an individual `*BenchMarkTest` class from the IDE — `AbstractBaseBenchmark` invokes the JMH `Runner` inside the `@Test` method.

## Gotchas

- **JMH annotation processor is required** (`jmh-generator-annprocess` at `provided` scope). IDE setup must enable annotation processing for this module or `@Benchmark` methods won't be picked up.
- **Incremental compilation is disabled** in this module's pom because JMH's processor regenerates everything — do not re-enable.
- **Results are noisy** on non-dedicated hardware. Don't ship benchmark deltas from a laptop as evidence of a performance regression / improvement.
- **Module is excluded from the release tarball** (it's a developer tool). It is in the root reactor build so `mvn compile` covers it, but its `@Test` entry points are fast and harmless.

## Related modules

- `dolphinscheduler-extract-base` — the RPC framework under benchmark.
