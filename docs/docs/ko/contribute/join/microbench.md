# 마이크로 벤치마크 공지

모든 최적화는 데이터 검증을 기반으로 이루어져야 하며, 블라인드 최적화는 거부됩니다.이를 기반으로 MicroBench 모듈을 제공합니다.

MicroBench 모듈은 OpenJDK JMH 구성 요소(HotSpot에서 권장하는 벤치마크 테스트 프로그램)를 기반으로 합니다.벤치마킹을 시작할 때 추가 종속성이 필요하지 않습니다.

Java MicroBenchmark Harness인 JMH는 코드 마이크로벤치마크 테스트 전용 도구 모음입니다.마이크로 벤치마크란 무엇입니까?간단히 말해서 이는 마이크로초 단위의 정확도를 갖춘 메서드 수준 벤치마크 테스트를 기반으로 합니다.핫 분석법을 찾고 해당 분석법의 성능을 더욱 최적화하려는 경우 JMH를 사용하여 최적화된 결과를 정량적으로 분석할 수 있습니다.

### Java 벤치마크 테스트에서 주의할 몇 가지 사항:

- 쓸모없는 코드가 테스트 메소드에 들어가는 것을 방지합니다.

- 동시 테스트.

- 테스트 결과가 제시됩니다.

### JMH의 일반적인 적용 시나리오는 다음과 같습니다.

- 1: 핫스팟 기능의 최적화 효과를 정량적으로 분석

- 2: 함수를 얼마나 오랫동안 실행해야 하는지, 실행 시간과 입력변수의 상관관계를 정량적으로 알고 싶다.

- 3: 함수의 여러 구현 비교

DolphinScheduler-MicroBench는 AbstractBaseBenchmark를 제공하며, 여기에서 상속하고 벤치마크 코드를 작성할 수 있으며 AbstractMicroBenchmark는 JUnit 모드에서 실행을 보장할 수 있습니다.

### 맞춤형 작동 매개변수

기본 AbstractMicrobenchmark 구성은 다음과 같습니다.

준비 시간 10(warmupIterations)

테스트 수 10(measureIterations)

포크 수량 2(forkCount)

시작 시 이러한 매개변수를 지정할 수 있습니다. -DmeasureIterations, -DperfReportDir(출력 벤치마크 테스트 결과 파일 디렉터리), -DwarmupIterations, -DforkCount

### DolphinScheduler-MicroBench 소개

일반적으로 테스트를 실행할 때 더 적은 주기를 사용하는 것은 권장되지 않습니다.그러나 벤치마크 테스트 중 작업을 확인하는 데는 테스트 횟수가 적습니다.검증이 끝난 후 다수의 벤치마크 테스트를 실행합니다.```java
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 4, time = 1)
@State(Scope.Benchmark)
public class EnumBenchMark extends AbstractBaseBenchmark {

}
````

이를 통해 메서드 수준이나 클래스 수준에서 벤치마크를 실행할 수 있습니다.명령줄 매개변수는 주석의 매개변수보다 우선 적용됩니다.```java
@Benchmark // Method annotation, indicating that the method is an object that needs to be benchmarked.
@BenchmarkMode(Mode.AverageTime) // Optional benchmark test mode is obtained through enumeration
@OutputTimeUnit(TimeUnit.MICROSECONDS) // Output time unit
public void enumStaticMapTest() {
    TestTypeEnum.newGetNameByType(testNum);
}
````

벤치마크 테스트가 작성되면 이를 실행하여 특정 테스트 조건을 볼 수 있습니다. (실제 결과는 시스템 구성에 따라 다릅니다.)

먼저, 코드를 워밍업하고```java
# Warmup Iteration   1: 0.007 us/op
# Warmup Iteration   2: 0.008 us/op
Iteration   1: 0.004 us/op
Iteration   2: 0.004 us/op
Iteration   3: 0.004 us/op
Iteration   4: 0.004 us/op
````

워밍업 후에는 일반적으로 다음과 같은 결과를 얻습니다.```java
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
````

OpenJDK는 공식적으로 많은 샘플 코드를 제공했으며 관심 있는 학생들은 스스로 JMH를 쿼리하고 배울 수 있습니다.