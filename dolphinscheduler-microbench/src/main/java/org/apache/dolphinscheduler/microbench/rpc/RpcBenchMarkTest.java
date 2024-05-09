/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.microbench.rpc;

import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.SpringServerMethodInvokerDiscovery;
import org.apache.dolphinscheduler.microbench.base.AbstractBaseBenchmark;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Slf4j
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime})
public class RpcBenchMarkTest extends AbstractBaseBenchmark {

    private SpringServerMethodInvokerDiscovery springServerMethodInvokerDiscovery;

    private IService iService;

    @Setup
    public void before() {
        NettyServerConfig nettyServerConfig =
                NettyServerConfig.builder().serverName("NettyRemotingServer").listenPort(12345).build();
        springServerMethodInvokerDiscovery = new SpringServerMethodInvokerDiscovery(nettyServerConfig);
        springServerMethodInvokerDiscovery.postProcessAfterInitialization(new IServiceImpl(), "iServiceImpl");
        springServerMethodInvokerDiscovery.start();
        iService =
                SingletonJdkDynamicRpcClientProxyFactory.getProxyClient("localhost:12345", IService.class);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void sendTest(Blackhole bh) {
        String pong = iService.ping("ping");
        bh.consume(pong);
    }

    @TearDown
    public void after() {
        springServerMethodInvokerDiscovery.close();
    }
}
