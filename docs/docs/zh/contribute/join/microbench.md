# 微基准测试须知

所有的优化必须建立在数据印证的基础上，拒绝盲目优化。基于此，我们提供了MicroBench模块。

MicroBench模块是基于OpenJDK JMH构件的（HotSpot的推荐基准测试方案）。当你开始基准测试时，你不需要额外的依赖。

JMH，即Java MicroBenchmark Harness，是专门用于代码微基准测试的工具套件。何谓Micro Benchmark呢？简单的来说就是基于方法层面的基准测试，精度可以达到微秒级。当你定位到热点方法，希望进一步优化方法性能的时候，就可以使用JMH对优化的结果进行量化的分析。

### Java基准测试需要注意的几个点：

* 防止无用代码进入测试方法中。

* 并发测试。

* 测试结果呈现。

### JMH比较典型的应用场景有：

* 1:定量分析某个热点函数的优化效果

* 2:想定量地知道某个函数需要执行多长时间，以及执行时间和输入变量的相关性

* 3:对比一个函数的多种实现方式

DolphinScheduler-MicroBench提供了AbstractBaseBenchmark,你可以在其基础上继承，编写你的基准测试代码，AbstractMicroBenchmark能保证以JUnit的方式运行。

### 定制运行参数

默认的AbstractMicrobenchmark配置是

Warmup次数 10（warmupIterations）

测试次数 10（measureIterations）

Fork数量 2 （forkCount）

你可以在启动的时候指定这些参数，-DmeasureIterations、-DperfReportDir（输出基准测试结果文件目录）、-DwarmupIterations、-DforkCount

### DolphinScheduler-MicroBench 介绍

通常并不建议跑测试时，用较少的循环次数，但是较少的次数有助于确认基准测试时工作的，在确认结束后，再运行大量的基准测试。

```java
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 4, time = 1)
@State(Scope.Benchmark)
public class EnumBenchMark extends AbstractBaseBenchmark {

}
```

这可以以方法级别或者类级别来运行基准测试，命令行的参数会覆盖annotation上的参数。

```java
@Benchmark //方法注解，表示该方法是需要进行 benchmark 的对象。
@BenchmarkMode(Mode.AverageTime) //可选基准测试模式通过枚举Mode得到
@OutputTimeUnit(TimeUnit.MICROSECONDS) // 输出的时间单位
public void enumStaticMapTest() {
    TestTypeEnum.newGetNameByType(testNum);
}
```

当你的基准测试编写完成后，你可以运行它查看具体的测试情况：（实际结果取决于你的系统配置情况）

首先它会对我们的代码进行预热，

```
# Warmup Iteration   1: 0.007 us/op
# Warmup Iteration   2: 0.008 us/op
Iteration   1: 0.004 us/op
Iteration   2: 0.004 us/op
Iteration   3: 0.004 us/op
Iteration   4: 0.004 us/op
```

在经过预热后，我们通常会得到如下结果

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

OpenJDK官方给了很多样例代码，有兴趣的同学可以自己查询并学习JMH：[OpenJDK-JMH-Example](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/)
