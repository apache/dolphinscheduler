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

import java.util.function.Supplier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;

public final class ProcessInstanceMetrics {

    private ProcessInstanceMetrics() {
        throw new UnsupportedOperationException("Utility class");
    }

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

    public static synchronized void registerProcessInstanceRunningGauge(Supplier<Number> function) {
        Gauge.builder("ds.workflow.instance.running", function)
                .description("The current running process instance count")
                .register(Metrics.globalRegistry);
    }

    public static void incProcessInstanceSubmit() {
        PROCESS_INSTANCE_SUBMIT_COUNTER.increment();
    }

    public static void incProcessInstanceTimeout() {
        PROCESS_INSTANCE_TIMEOUT_COUNTER.increment();
    }

    public static void incProcessInstanceFinish() {
        PROCESS_INSTANCE_FINISH_COUNTER.increment();
    }

    public static void incProcessInstanceSuccess() {
        PROCESS_INSTANCE_SUCCESS_COUNTER.increment();
    }

    public static void incProcessInstanceFailure() {
        PROCESS_INSTANCE_FAILURE_COUNTER.increment();
    }

    public static void incProcessInstanceStop() {
        PROCESS_INSTANCE_STOP_COUNTER.increment();
    }

    public static void incProcessInstanceFailover() {
        PROCESS_INSTANCE_FAILOVER_COUNTER.increment();
    }

}
