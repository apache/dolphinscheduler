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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.facebook.presto.jdbc.internal.guava.collect.ImmutableSet;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;


public final class TaskMetrics {
    private TaskMetrics() {
        throw new UnsupportedOperationException("Utility class");
    }


    private static Map<String, Counter> TASK_INSTANCE_COUNTERS = new HashMap<>();

    private static final Set<String> TASK_INSTANCE_STATES = ImmutableSet.of(
            "submit", "timeout", "finish", "failover", "retry", "dispatch");

    private static final Set<String> TASK_INSTANCE_STATUSES = ImmutableSet.of("success", "fail", "stop");

    static {
        for (final String state : TASK_INSTANCE_STATES) {
            TASK_INSTANCE_COUNTERS.put(
                    state,
                    Counter.builder("ds.task.instance.count")
                            .tag("state", state)
                            .description(String.format("Process instance %s total count", state))
                            .register(Metrics.globalRegistry)
            );
        }
    }

    static {
        for (final String status : TASK_INSTANCE_STATUSES) {
            TASK_INSTANCE_COUNTERS.put(
                    status,
                    Counter.builder("ds.task.instance.count")
                            .tag("status", status)
                            .description(String.format("Process instance %s total count", status))
                            .register(Metrics.globalRegistry)
            );
        }
    }

    private static final Counter TASK_SUBMIT_COUNTER =
            Counter.builder("ds.task.submit.count")
                    .description("Task submit total count")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_FINISH_COUNTER =
            Counter.builder("ds.task.finish.count")
                    .description("Task finish total count")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_SUCCESS_COUNTER =
            Counter.builder("ds.task.success.count")
                    .description("Task success total count")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_FAILURE_COUNTER =
            Counter.builder("ds.task.failure.count")
                    .description("Task failure total count")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_TIMEOUT_COUNTER =
            Counter.builder("ds.task.timeout.count")
                    .description("Task timeout total count")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_RETRY_COUNTER =
            Counter.builder("ds.task.retry.count")
                    .description("Task retry total count")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_STOP_COUNTER =
            Counter.builder("ds.task.stop.count")
                    .description("Task stop total count")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_FAILOVER_COUNTER =
            Counter.builder("ds.task.failover.count")
                    .description("Task failover total count")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_DISPATCH_COUNTER =
            Counter.builder("ds.task.dispatch.count")
                    .description("Task dispatch count")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_DISPATCHER_FAILED =
            Counter.builder("ds.task.dispatch.failure.count")
                    .description("Task dispatch failures count, retried ones included")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_DISPATCH_ERROR =
            Counter.builder("ds.task.dispatch.error.count")
                    .description("Number of errors during task dispatch")
                    .register(Metrics.globalRegistry);

    public static void incTaskSubmit() {
        TASK_SUBMIT_COUNTER.increment();
    }

    public synchronized static void registerTaskPrepared(Supplier<Number> consumer) {
        Gauge.builder("ds.task.prepared", consumer)
                .description("Task prepared count")
                .register(Metrics.globalRegistry);
    }

    public static void incTaskDispatchFailed(int failedCount) {
        TASK_DISPATCHER_FAILED.increment(failedCount);
    }

    public static void incTaskDispatchError() {
        TASK_DISPATCH_ERROR.increment();
    }

    public static void incTaskDispatch() {
        TASK_DISPATCH_COUNTER.increment();
    }

    public static void incTaskInstanceByState(final String state) {
        TASK_INSTANCE_COUNTERS.get(state).increment();
    }

    public static void incTaskInstanceByStatus(final String status) {
        TASK_INSTANCE_COUNTERS.get(status).increment();
    }

}
