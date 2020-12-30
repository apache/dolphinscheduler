package org.apache.dolphinscheduler.microbench.serializer;

import org.apache.dolphinscheduler.microbench.base.AbstractBaseBenchmark;
import org.apache.dolphinscheduler.microbench.serializer.BeanTestProto.TestBeanProto;
import org.apache.dolphinscheduler.microbench.serializer.kyro.KryoSerialization;
import org.apache.dolphinscheduler.microbench.serializer.protostuff.ProtoStuffSerialization;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @CalvinKirs
 * @date 2020-12-29 17:38
 */
@Warmup(iterations = 10, time = 2)
@Measurement(iterations = 10, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class SerializerBenchMark extends AbstractBaseBenchmark {
    private TestBean test;
    private ProtoStuffSerialization protoStuffSerialization = new ProtoStuffSerialization();
    private KryoSerialization kryoSerialization = new KryoSerialization();

    BeanTestProto.TestBeanProto.Builder testProtoBeanBuilder;
    BeanTestProto.TestBeanProto testProtoData;

    @Setup
    public void setupBenchmark() {
        test = new TestBean();

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", "4");
        paramsMap.put("expireTime", "2019-12-18 00:00:00");
        paramsMap.put("token", "607f5aeaaa2093dbdff5d5522ce00510");
        paramsMap.put("usrId", "4");
        paramsMap.put("startTime", "2019-12-18 00:00:00");
        paramsMap.put("uuid", "607f5aeaaa2093dbdff5d5522ce00510");
        paramsMap.put("createUserId", "4");
        paramsMap.put("updateTime", "2019-12-18 00:00:00");
        paramsMap.put("password", "607f5aeaaa2093dbdff5d5522ce00510");
        paramsMap.put("updateUserId", "4");
        paramsMap.put("messyTime", "2019-12-18 00:00:00");
        paramsMap.put("kids", "607f5aeaaa2093dbdff5d5522ce00510");
        paramsMap.put("usrId", "4");
        paramsMap.put("startTime", "2019-12-18 00:00:00");
        paramsMap.put("uuid", "607f5aeaaa2093dbdff5d5522ce00510");
        paramsMap.put("createUserId", "4");
        paramsMap.put("updateTime", "2019-12-18 00:00:00");
        paramsMap.put("password", "607f5aeaaa2093dbdff5d5522ce00510");
        paramsMap.put("updateUserId", "4");
        paramsMap.put("messyTime", "2019-12-18 00:00:00");
        paramsMap.put("kids", "607f5aeaaa2093dbdff5d5522ce00510");
        paramsMap.put("usrId", "4");
        paramsMap.put("startTime", "2019-12-18 00:00:00");
        paramsMap.put("uuid", "607f5aeaaa2093dbdff5d5522ce00510");
        paramsMap.put("createUserId", "4");
        paramsMap.put("updateTime", "2019-12-18 00:00:00");
        paramsMap.put("password", "607f5aeaaa2093dbdff5d5522ce00510");
        paramsMap.put("updateUserId", "4");
        paramsMap.put("messyTime", "2019-12-18 00:00:00");
        paramsMap.put("kids", "607f5aeaaa2093dbdff5d5522ce00510");
        test.setItems(paramsMap);
        test.setPsd("HJJJLDFGHJKCVBNM<FGHJKLRTYUIOVBNMJK<VBNM阿帕奇海豚调度");
        test.setAges(123456789.1234);
        test.setUser("アパッチドルフィンディスパッチ");
        test.setIds(Arrays.asList("阿帕奇海豚调度", "Apache Dolphin Scheduler", "アパッチドルフィンディスパッチ", "Ukuhanjiswa kweApache Dolphin", "Envoi Apache Dolphin", "Apache Dolphin Scheduler", "アパッチドルフィンディスパッチ",
            "Ukuhanjiswa kweApache Dolphin", "Envoi Apache Dolphin"));

        testProtoBeanBuilder = BeanTestProto.TestBeanProto.newBuilder();
        testProtoBeanBuilder.setAges(123456789.1234);
        testProtoBeanBuilder.setPsd("HJJJLDFGHJKCVBNM<FGHJKLRTYUIOVBNMJK<VBNM阿帕奇海豚调度");
        testProtoBeanBuilder.setUser("アパッチドルフィンディスパッチ");
        testProtoBeanBuilder
            .addAllIds(Arrays.asList("阿帕奇海豚调度", "Apache Dolphin Scheduler", "アパッチドルフィンディスパッチ", "Ukuhanjiswa kweApache Dolphin", "Envoi Apache Dolphin", "Apache Dolphin Scheduler", "アパッチドルフィンディスパッチ",
                "Ukuhanjiswa kweApache Dolphin", "Envoi Apache Dolphin"));
        testProtoBeanBuilder.putAllItems(paramsMap);
    }

    @Benchmark
    public void testProtoStuff() {
        byte[] data = protoStuffSerialization.serialize(test);
        protoStuffSerialization.deserialize(data, TestBean.class);
    }

    @Benchmark
    public void testKryo() {
        byte[] data = kryoSerialization.serialize(test);
        kryoSerialization.deserialize(data, TestBean.class);

    }

    @Benchmark
    public void testProtoBuf() {

        testProtoData = testProtoBeanBuilder.build();
        byte[] data = testProtoData.toByteArray();
        try {
            TestBeanProto.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }


}
