# Micro BenchMark Notice

All optimization must be based on data verification, and blind optimization is rejected. Based on this, we provide the MicroBench module.

The MicroBench module is based on the OpenJDK JMH component (HotSpot's recommended benchmark test program). When you start benchmarking, you don't need additional dependencies.

JMH, the Java MicroBenchmark Harness, is a tool suite dedicated to code microbenchmark testing. What is Micro Benchmark? Simply put, it is based on method-level benchmark testing, with an accuracy of microseconds. When you locate a hot method and want to further optimize the performance of the method, you can use JMH to quantitatively analyze the optimized results.

### Several points to note in Java benchmark testing:

- Prevent useless code from entering the test method.

- Concurrent testing.

- The test results are presented.

### Typical application scenarios of JMH are:

- 1: Quantitatively analyze the optimization effect of a hotspot function

- 2: Want to quantitatively know how long a function needs to be executed, and the correlation between execution time and input variables

- 3: Compare multiple implementations of a function

DolphinScheduler-MicroBench provides AbstractBaseBenchmark, you can inherit from it, write your benchmark code, AbstractMicroBenchmark can guarantee to run in JUnit mode.

### Customized operating parameters

The default AbstractMicrobenchmark configuration is

Warmup times 10 (warmupIterations)

Number of tests 10 (measureIterations)

Fork quantity 2 (forkCount)

You can specify these parameters at startup，-DmeasureIterations, -DperfReportDir (output benchmark test result file directory), -DwarmupIterations, -DforkCount

### DolphinScheduler-MicroBench Introduction

It is generally not recommended to use fewer cycles when running tests. However, a smaller number of tests helps to verify the work during the benchmark test. After the verification is over, run a large number of benchmark tests.

```java
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 4, time = 1)
@State(Scope.Benchmark)
public class EnumBenchMark extends AbstractBaseBenchmark {

}
```

This can run benchmarks at the method level or the class level. Command line parameters will override the parameters on the annotation.

```java
@Benchmark // Method annotation, indicating that the method is an object that needs to be benchmarked.
@BenchmarkMode(Mode.AverageTime) // Optional benchmark test mode is obtained through enumeration
@OutputTimeUnit(TimeUnit.MICROSECONDS) // Output time unit
public void enumStaticMapTest() {
    TestTypeEnum.newGetNameByType(testNum);
}
```

When your benchmark test is written, you can run it to view the specific test conditions: (The actual results depend on your system configuration)

First, it will warm up our code,

```java
# Warmup Iteration   1: 0.007 us/op
# Warmup Iteration   2: 0.008 us/op
Iteration   1: 0.004 us/op
Iteration   2: 0.004 us/op
Iteration   3: 0.004 us/op
Iteration   4: 0.004 us/op
```

After warmup, we usually get the following results

```java
Benchmark                        (testNum)   Mode  Cnt          Score           Error  Units
EnumBenchMark.simpleTest               101  thrpt    8  428750972.826 ±  66511362.350  ops/s
EnumBenchMark.simpleTest               108  thrpt    8  299615240.337 ± 290089561.671  ops/s
EnumBenchMark.simpleTest               103  thrpt    8  288423221.721 ± 130542990.747  ops/s
EnumBenchMark.simpleTest               104  thrpt    8  236811792.152 ± 155355935.479  ops/s
EnumBenchMark.simpleTest               105  thrpt    8  472247775.246 ±  45769877.951  ops/s
EnumBenchMark.simpleTest               103  thrpt    8  455473025.252 ±  61212956.944  ops/s
EnumBenchMark.enumStaticMapTest        101   avgt    8          0.006 ±         0.003  us/op
EnumBenchMark.enumStaticMapTest        108   avgt    8          0.005 ±         0.002  us/op
EnumBenchMark.enumStaticMapTest        103   avgt    8          0.006 ±         0.005  us/op
EnumBenchMark.enumStaticMapTest        104   avgt    8          0.006 ±         0.004  us/op
EnumBenchMark.enumStaticMapTest        105   avgt    8          0.004 ±         0.001  us/op
EnumBenchMark.enumStaticMapTest        103   avgt    8          0.004 ±         0.001  us/op
EnumBenchMark.enumValuesTest           101   avgt    8          0.011 ±         0.004  us/op
EnumBenchMark.enumValuesTest           108   avgt    8          0.025 ±         0.016  us/op
EnumBenchMark.enumValuesTest           103   avgt    8          0.019 ±         0.010  us/op
EnumBenchMark.enumValuesTest           104   avgt    8          0.018 ±         0.018  us/op
EnumBenchMark.enumValuesTest           105   avgt    8          0.014 ±         0.012  us/op
EnumBenchMark.enumValuesTest           103   avgt    8          0.012 ±         0.009  us/op
```

OpenJDK officially gave a lot of sample codes, interested students can query and learn JMH by themselves:[OpenJDK-JMH-Example](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/)
