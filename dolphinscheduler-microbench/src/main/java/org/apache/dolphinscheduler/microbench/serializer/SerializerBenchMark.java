package org.apache.dolphinscheduler.microbench.serializer;

import org.apache.dolphinscheduler.microbench.base.AbstractBaseBenchmark;
import org.apache.dolphinscheduler.microbench.serializer.kyro.KryoSerialization;
import org.apache.dolphinscheduler.microbench.serializer.protostuff.ProtoStuffSerialization;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandContext;
import org.apache.dolphinscheduler.remote.command.CommandType;

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
    Command command;
    ProtoStuffSerialization protoStuffSerialization = new ProtoStuffSerialization();
    KryoSerialization kryoSerialization = new KryoSerialization();

    @Setup
    public void setupBenchmark() {
        command = new Command();
        CommandContext context = new CommandContext();
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("userId", "4");
        paramsMap.put("expireTime", "2019-12-18 00:00:00");
        paramsMap.put("token", "607f5aeaaa2093dbdff5d5522ce00510");
        context.setItems(paramsMap);
        command.setContext(context);
        command.setType(CommandType.DB_TASK_ACK);
    }

    @Benchmark
    public void testProtostuffSerial() {
        byte[] data = protoStuffSerialization.serialize(command);
        protoStuffSerialization.deserialize(data, Command.class);
    }

    @Benchmark
    public void testKryoSerial() {
        byte[] data = kryoSerialization.serialize(command);
        kryoSerialization.deserialize(data, Command.class);
    }

}
