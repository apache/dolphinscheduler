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

package org.apache.dolphinscheduler.server.master.metrics;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

public final class ProcessInstanceMetrics {

    private ProcessInstanceMetrics() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static Map<String, Counter> PROCESS_INSTANCE_COUNTERS = new HashMap<>();

    private static final Set<String> PROCESS_INSTANCE_STATES = ImmutableSet.of(
            "submit", "timeout", "finish", "failover");

    private static final Set<String> PROCESS_INSTANCE_STATUSES = ImmutableSet.of("success", "fail", "stop");

    static {
        for (final String state : PROCESS_INSTANCE_STATES) {
            PROCESS_INSTANCE_COUNTERS.put(
                    state,
                    Counter.builder("ds.workflow.instance.count")
                            .tag("state", state)
                            .description(String.format("Process instance %s total count", state))
                            .register(Metrics.globalRegistry)
            );
        }
    }

    static {
        for (final String status : PROCESS_INSTANCE_STATUSES) {
            PROCESS_INSTANCE_COUNTERS.put(
                    status,
                    Counter.builder("ds.workflow.instance.count")
                            .tag("status", status)
                            .description(String.format("Process instance %s total count", status))
                            .register(Metrics.globalRegistry)
            );
        }
    }

    private static final Timer COMMAND_QUERY_TIMETER =
        Timer.builder("ds.workflow.command.query.duration")
            .description("Command query duration")
            .register(Metrics.globalRegistry);

    private static final Timer PROCESS_INSTANCE_GENERATE_TIMER =
        Timer.builder("ds.workflow.instance.generate.duration")
            .description("Process instance generated duration")
            .register(Metrics.globalRegistry);

    private static final Counter PROCESS_INSTANCE_SUBMIT_COUNTER =
        Counter.builder("ds.workflow.instance.submit.count")
            .description("Process instance submit total count")
            .register(Metrics.globalRegistry);

    private static final Counter PROCESS_INSTANCE_TIMEOUT_COUNTER =
        Counter.builder("ds.workflow.instance.timeout.count")
            .description("Process instance timeout total count")
            .register(Metrics.globalRegistry);

    private static final Counter PROCESS_INSTANCE_FINISH_COUNTER =
            Counter.builder("ds.workflow.instance.finish.count")
                    .description("Process instance finish total count")
                    .register(Metrics.globalRegistry);

    private static final Counter PROCESS_INSTANCE_SUCCESS_COUNTER =
            Counter.builder("ds.workflow.instance.success.count")
                    .description("Process instance success total count")
                    .register(Metrics.globalRegistry);

    private static final Counter PROCESS_INSTANCE_FAILURE_COUNTER =
            Counter.builder("ds.workflow.instance.failure.count")
                    .description("Process instance failure total count")
                    .register(Metrics.globalRegistry);

    private static final Counter PROCESS_INSTANCE_STOP_COUNTER =
        Counter.builder("ds.workflow.instance.stop.count")
            .description("Process instance stop total count")
            .register(Metrics.globalRegistry);

    private static final Counter PROCESS_INSTANCE_FAILOVER_COUNTER =
        Counter.builder("ds.workflow.instance.failover.count")
            .description("Process instance failover total count")
            .register(Metrics.globalRegistry);

    public static void recordCommandQueryTime(long milliseconds) {
        COMMAND_QUERY_TIMETER.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public static void recordProcessInstanceGenerateTime(long milliseconds) {
        PROCESS_INSTANCE_GENERATE_TIMER.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public static synchronized void registerProcessInstanceRunningGauge(Supplier<Number> function) {
        Gauge.builder("ds.workflow.instance.running", function)
            .description("The current running process instance count")
            .register(Metrics.globalRegistry);
    }

    public static synchronized void registerProcessInstanceResubmitGauge(Supplier<Number> function) {
        Gauge.builder("ds.workflow.instance.resubmit", function)
            .description("The current process instance need to resubmit count")
            .register(Metrics.globalRegistry);
    }

    public static void incProcessInstanceByState(final String state) {
        PROCESS_INSTANCE_COUNTERS.get(state).increment();
    }

    public static void incProcessInstanceByStatus(final String status) {
        PROCESS_INSTANCE_COUNTERS.get(status).increment();
    }
}
